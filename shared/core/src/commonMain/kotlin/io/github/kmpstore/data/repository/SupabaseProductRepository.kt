package io.github.kmpstore.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.kmpstore.CategoryQueries
import io.github.kmpstore.ProductQueries
import io.github.kmpstore.Category_productsQueries
import io.github.kmpstore.data.remote.model.CategoryProductsDto
import io.github.kmpstore.data.remote.model.StoreSyncDto
import io.github.kmpstore.data.remote.model.ProductDto
import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.domain.model.Resource
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class SupabaseProductRepository(
    private val supabase: SupabaseClient,
    private val productQueries: ProductQueries,
    private val categoryQueries: CategoryQueries,
    private val categoryProductsQueries: Category_productsQueries
) : ProductRepository {

    private val missingProductIds = mutableSetOf<String>()
    private val missingCategoryIds = mutableSetOf<String>()
    private val activeProductFetches = mutableSetOf<String>()
    private val activeCategoryFetches = mutableSetOf<String>()

    override fun getStoreFront(): Flow<List<Category>> {
        val categoriesFlow = categoryQueries.selectAllCategories()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .distinctUntilChanged()

        val recursiveProductsMapFlow = productQueries.selectAllCategoryProductsRecursive()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                // Group the products by their calculated root category ID
                list.groupBy(
                    keySelector = { it.category_id },
                    valueTransform = { Product(it) }
                )
            }
            .distinctUntilChanged()

        return combine(categoriesFlow, recursiveProductsMapFlow) { categories, productsMap ->
            categories.map { dbCategory ->
                Category(
                    id = dbCategory.id,
                    name = dbCategory.name,
                    slug = dbCategory.slug,
                    parentId = dbCategory.parent_id,
                    // No more manual tree calculation! The DB already figured it out.
                    products = productsMap[dbCategory.id].orEmpty().distinctBy { it.id }
                )
            }
        }
    }

    override fun getProductsByCategory(categoryId: String): Flow<ProductRepository.productList> {
        return productQueries.selectProductsByCategoryIdRecursive(categoryId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                when {
                    list.isNotEmpty() -> Resource.Success(list.map { Product(it) })
                    missingCategoryIds.contains(categoryId) -> Resource.NotFound
                    else -> Resource.Loading
                }
            }.onStart { refreshCategory(categoryId) }
    }

    override fun getProductById(id: String): Flow<Resource<Product>> {
        return productQueries.selectProductById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map {
                when {
                    it != null -> Resource.Success(Product(it))
                    missingProductIds.contains(id) -> Resource.NotFound
                    else -> {
                        refreshProduct(id)
                        Resource.Loading
                    }
                }
            }.onStart { refreshProduct(id) }
    }

    override suspend fun refreshProducts(): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            // 1. Fetch data from Supabase
            val payload = supabase.postgrest.rpc("get_store_sync_payload")
                .decodeAs<StoreSyncDto>()
            // 2. Perform Atomic Transaction
            productQueries.transaction {
                //Clear old junction data to avoid stale relationships
                categoryProductsQueries.deleteAllCategoryProducts()

                payload.categories.forEach { category ->
                    categoryQueries.insertCategory(
                        id = category.id,
                        name = category.name,
                        slug = category.slug,
                        parent_id = category.parentId
                    )
                }
                insertCatalogAndJunctions(payload.products, payload.junctions)
            }
        }
    }

    override suspend fun refreshProduct(productId: String): Result<Unit> = withContext(Dispatchers.Default) {
        if (activeProductFetches.contains(productId) || missingProductIds.contains(productId)) {
            return@withContext Result.success(Unit)
        }

        activeProductFetches.add(productId)
        val result = runCatching {
            // 1. Fetch only the specific product from Supabase
            val remoteProduct = supabase.from("products")
                .select { filter { eq("id", productId) } }
                .decodeSingleOrNull<ProductDto>()

            if (remoteProduct == null) {
                missingProductIds.add(productId)
                return@runCatching
            } else {
                productQueries.insertProduct(
                    id = remoteProduct.id,
                    name = remoteProduct.name,
                    description = remoteProduct.description,
                    image_url = remoteProduct.mainImage,
                    price = remoteProduct.price,
                    currency = remoteProduct.currency,
                    price_id = remoteProduct.priceId,
                    stock = remoteProduct.stock
                )
            }
        }
        activeProductFetches.remove(productId)
        result
    }

    private suspend fun refreshCategory(categoryId: String): Result<Unit> = withContext(Dispatchers.Default) {
        if (activeCategoryFetches.contains(categoryId) || missingCategoryIds.contains(categoryId)) {
            return@withContext Result.success(Unit)
        }
        activeCategoryFetches.add(categoryId)
        val result = runCatching {
            val payload = supabase.postgrest.rpc(
                function = "get_category_sync_payload",
                parameters = mapOf("requested_category_id" to categoryId)
            ).decodeSingle<StoreSyncDto>()

            if (payload.categories.isEmpty()) {
                missingCategoryIds.add(categoryId)
                return@withContext Result.success(Unit)
            }
            productQueries.transaction {
                categoryProductsQueries.deleteCategoryProductsByCategoryId(categoryId)
                // Update local category table
                payload.categories.forEach { category ->
                    categoryQueries.insertCategory(
                        id = category.id,
                        name = category.name,
                        slug = category.slug,
                        parent_id = category.parentId
                    )
                }
                insertCatalogAndJunctions(payload.products, payload.junctions)
            }
        }
        activeCategoryFetches.remove(categoryId)
        result
    }

    private suspend fun insertCatalogAndJunctions(products: List<ProductDto>, junctions: List<CategoryProductsDto>) {
        products.forEach { product ->
            productQueries.insertProduct(
                id = product.id,
                name = product.name,
                description = product.description,
                image_url = product.mainImage,
                price = product.price,
                currency = product.currency,
                price_id = product.priceId,
                stock = product.stock
            )
        }
        junctions.forEach { item ->
            categoryProductsQueries.insertCategoryProduct(
                category_id = item.categoryId,
                product_id = item.productId
            )
        }
    }
}