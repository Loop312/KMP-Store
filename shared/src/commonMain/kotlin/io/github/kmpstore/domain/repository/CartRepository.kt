package io.github.kmpstore.domain.repository

import io.github.kmpstore.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(): Flow<List<CartItem>>
    fun getTotalPrice(): Flow<Long>
    suspend fun addToCart(productId: String, quantity: Long)
    suspend fun removeFromCart(productId: String)
    suspend fun clearCart()
}