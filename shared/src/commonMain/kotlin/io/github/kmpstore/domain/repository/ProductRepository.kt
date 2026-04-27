package io.github.kmpstore.domain.repository

import io.github.kmpstore.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getStoreFront(): Flow<List<Category>> // Returns topics with their products
    suspend fun refreshProducts(): Result<Unit>
}