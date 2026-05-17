package io.github.kmpstore.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.kmpstore.CategoryQueries
import io.github.kmpstore.ProductQueries
import io.github.kmpstore.Category_productsQueries
import io.github.kmpstore.data.remote.model.CategoryDto
import io.github.kmpstore.data.remote.model.CategoryProductsDto
import io.github.kmpstore.data.remote.model.StoreCatalogDto
import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.domain.model.Resource
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

        val productMapFlow = productQueries.selectAllProducts()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.associateBy { it.id } }
            .distinctUntilChanged()

        val junctionsFlow = categoryProductsQueries.selectAllCategoryProducts()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .distinctUntilChanged()

        return combine(categoriesFlow, productMapFlow, junctionsFlow) { categories, productMap, junctions ->
            // Group junction records by category ID
            val junctionsByCategory = junctions.groupBy { it.category_id }
            // Build the Category objects
            categories.map { dbCategory ->
                val productIdsForCategory = junctionsByCategory[dbCategory.id].orEmpty()
                Category(
                    id = dbCategory.id,
                    name = dbCategory.name, // Changed from title to name
                    slug = dbCategory.slug,
                    products = productIdsForCategory.mapNotNull { junction ->
                        productMap[junction.product_id]?.let { Product(it) }
                    }
                )
            }
        }
    }

    override fun getProductsByCategory(categoryId: String): Flow<ProductRepository.productList> {
        return productQueries.selectProductsByCategoryId(categoryId)
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
            val categoriesDeferred = async {
                supabase.from("categories").select().decodeList<CategoryDto>()
            }
            val junctionsDeferred = async {
                supabase.from("category_products").select().decodeList<CategoryProductsDto>()
            }
            val catalogDeferred = async {
                supabase.from("store_catalog").select().decodeList<StoreCatalogDto>()
            }
            val remoteCategories = categoriesDeferred.await()
            val remoteJunctions = junctionsDeferred.await()
            val remoteCatalog = catalogDeferred.await()

            // 2. Perform Atomic Transaction
            productQueries.transaction {
                //Clear old junction data to avoid stale relationships
                categoryProductsQueries.deleteAllCategoryProducts()

                remoteCategories.forEach { category ->
                    categoryQueries.insertCategory(
                        id = category.id,
                        name = category.name,
                        slug = category.slug,
                        description = category.description
                    )
                }
                insertCatalogAndJunctions(remoteCatalog, remoteJunctions)
            }
        }
    }

    private suspend fun refreshProduct(productId: String): Result<Unit> = withContext(Dispatchers.Default) {
        if (activeProductFetches.contains(productId) || missingProductIds.contains(productId)) {
            return@withContext Result.success(Unit)
        }

        activeProductFetches.add(productId)
        val result = runCatching {
            // 1. Fetch only the specific product from Supabase
            val remoteProduct = supabase.from("store_catalog")
                .select { filter { eq("product_id", productId) } }
                .decodeSingleOrNull<StoreCatalogDto>()

            if (remoteProduct == null) {
                missingProductIds.add(productId)
                return@runCatching
            } else {
                productQueries.insertProduct(
                    id = remoteProduct.productId,
                    name = remoteProduct.name,
                    description = remoteProduct.description,
                    image_url = remoteProduct.mainImage,
                    price = remoteProduct.unitAmount,
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
            val categoryDeferred = async {
                supabase.from("categories")
                    .select { filter { eq("id", categoryId) } }
                    .decodeSingleOrNull<CategoryDto>()
            }
            val junctionsDeferred = async {
                supabase.from("category_products")
                    .select { filter { eq("category_id", categoryId) } }
                    .decodeList<CategoryProductsDto>()
            }
            val catalogDeferred = async {
                supabase.from("store_catalog")
                    .select { filter { eq("category_id", categoryId) } }
                    .decodeList<StoreCatalogDto>()
            }

            val remoteCategory = categoryDeferred.await()
            val remoteJunctions = junctionsDeferred.await()
            val remoteCatalog = catalogDeferred.await()

            if (remoteCategory == null) {
                missingCategoryIds.add(categoryId)
                return@withContext Result.success(Unit)
            }
            productQueries.transaction {
                categoryProductsQueries.deleteCategoryProductsByCategoryId(categoryId)
                // Update local category table
                categoryQueries.insertCategory(
                    id = remoteCategory.id,
                    name = remoteCategory.name,
                    slug = remoteCategory.slug,
                    description = remoteCategory.description
                )
                insertCatalogAndJunctions(remoteCatalog, remoteJunctions)
            }
        }
        activeCategoryFetches.remove(categoryId)
        result
    }

    private suspend fun insertCatalogAndJunctions(catalog: List<StoreCatalogDto>, junctions: List<CategoryProductsDto>) {
        catalog.forEach { item ->
            productQueries.insertProduct(
                id = item.productId,
                name = item.name,
                description = item.description,
                image_url = item.mainImage,
                price = item.unitAmount,
                currency = item.currency,
                price_id = item.priceId,
                stock = item.stock
            )
        }

        junctions.forEach { junction ->
            categoryProductsQueries.insertCategoryProduct(
                category_id = junction.categoryId,
                product_id = junction.productId
            )
        }
    }
}