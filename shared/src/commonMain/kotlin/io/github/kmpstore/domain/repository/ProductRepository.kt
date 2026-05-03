package io.github.kmpstore.domain.repository

import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getStoreFront(): Flow<List<Category>> // Returns topics with their products
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product>
    suspend fun refreshProducts(): Result<Unit>
}