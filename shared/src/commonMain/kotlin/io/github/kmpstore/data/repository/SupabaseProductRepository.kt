package io.github.kmpstore.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.kmpstore.CategoryQueries
import io.github.kmpstore.ProductQueries
import io.github.kmpstore.data.remote.model.CategoryDto
import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class SupabaseProductRepository(
    private val supabase: SupabaseClient,
    private val productQueries: ProductQueries,
    private val categoryQueries: CategoryQueries
) : ProductRepository {

    override fun getStoreFront(): Flow<List<Category>> {
        // Single request to fetch categories AND their nested products
//        val response = supabase.from("categories")
//            .select(Columns.raw("*, products(*)"))
//            .decodeList<Category>()
//        println(response)
//        emit(response)

        // Observe categories and products as separate flows[cite: 13, 14]
        val categoriesFlow = categoryQueries.selectAllCategories()
            .asFlow()
            .mapToList(Dispatchers.Default)

        val productsFlow = productQueries.selectAllProducts()
            .asFlow()
            .mapToList(Dispatchers.Default)

        // Combine them in memory to avoid nested "executeAsList" calls
        return categoriesFlow.combine(productsFlow) { categories, allProducts ->
            categories.map { dbCategory ->
                Category(
                    id = dbCategory.id,
                    title = dbCategory.title,
                    products = allProducts
                        .filter { it.category_id == dbCategory.id }
                        .map { Product(it) }
                )
            }
        }
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        // Pure flow approach for the "See All" screen[cite: 14, 18]
        return productQueries.selectProductsByCategory(categoryId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { Product(it) } }
    }

    override fun getProductById(id: String): Flow<Product> {
//        val response = supabase.from("products").select {
//            filter {
//                eq("id", id)
//            }
//        }.decodeSingle<Product>()
//        println(response)
//        emit(response)
        return productQueries.selectProductById(id)
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { Product(it) }
    }

    override suspend fun refreshProducts(): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            // Fetch DTOs from Supabase[cite: 4, 15, 16]
            val remoteData = supabase.from("categories")
                .select(Columns.raw("*, products(*)"))
                .decodeList<CategoryDto>()

            // Transaction handles the insert/replace logic[cite: 13, 14]
            categoryQueries.transaction {
                remoteData.forEach { categoryDto ->
                    categoryQueries.insertCategory(
                        id = categoryDto.id,
                        title = categoryDto.title,
                        created_at = Clock.System.now().toString()
                    )

                    categoryDto.products.forEach { productDto ->
                        productQueries.insertProduct(
                            id = productDto.id,
                            category_id = productDto.categoryId,
                            name = productDto.name,
                            description = productDto.description,
                            price = productDto.price,
                            currency = productDto.currency,
                            image_url = productDto.imageUrl,
                            stock_quantity = 0,
                            created_at = Clock.System.now().toString()
                        )
                    }
                }
            }
        }
    }
}