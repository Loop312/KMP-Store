package io.github.kmpstore.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
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
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SupabaseProductRepository(
    private val supabase: SupabaseClient,
    private val productQueries: ProductQueries,
    private val categoryQueries: CategoryQueries,
    private val categoryProductsQueries: Category_productsQueries
) : ProductRepository {

    override fun getStoreFront(): Flow<List<Category>> {
        val categoriesFlow = categoryQueries.selectAllCategories()
            .asFlow()
            .mapToList(Dispatchers.Default)

        val productsFlow = productQueries.selectAllProducts()
            .asFlow()
            .mapToList(Dispatchers.Default)

        val junctionsFlow = categoryProductsQueries.selectAllCategoryProducts() // You'll need to add this query to category_products.sq
            .asFlow()
            .mapToList(Dispatchers.Default)

        return combine(categoriesFlow, productsFlow, junctionsFlow) { categories, allProducts, junctions ->
            // 1. Map products by ID for quick lookup
            val productMap = allProducts.associateBy { it.id }

            // 2. Group junction records by category ID
            val junctionsByCategory = junctions.groupBy { it.category_id }

            // 3. Build the Category objects
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

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        return productQueries.selectProductsByCategoryId(categoryId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { Product(it) } }
    }

    override fun getProductById(id: String): Flow<Product> {
        return productQueries.selectProductById(id)
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { Product(it) }
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

                remoteCatalog.forEach { item ->
                    productQueries.insertProduct(
                        id = item.productId,
                        name = item.name,
                        description = item.description,
                        image_url = item.mainImage,
                        price = item.unitAmount,
                        currency = item.currency,
                        price_id = item.priceId
                    )
                }

                remoteJunctions.forEach { junction ->
                    categoryProductsQueries.insertCategoryProduct(
                        category_id = junction.categoryId,
                        product_id = junction.productId
                    )
                }
            }
        }
    }
}