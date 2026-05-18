package io.github.kmpstore.domain.repository

import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    typealias productList = Resource<List<Product>>
    fun getStoreFront(): Flow<List<Category>> // Returns topics with their products
    fun getProductsByCategory(categoryId: String): Flow<productList>
    fun getProductById(id: String): Flow<Resource<Product>>
    suspend fun refreshProducts(): Result<Unit>
    suspend fun refreshProduct(productId: String): Result<Unit>
}