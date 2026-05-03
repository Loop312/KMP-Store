package io.github.kmpstore.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import io.github.kmpstore.Cart_itemQueries
import io.github.kmpstore.domain.model.CartItem
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.domain.repository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class RealCartRepository(
    private val cartItemQueries: Cart_itemQueries
) : CartRepository {
    override fun getCart(): Flow<List<CartItem>> {
        return cartItemQueries.selectCartDetails { id, name, description, price, imageUrl, categoryId, currency, quantity, _ ->
            CartItem(
                product = Product(
                    id = id,
                    name = name,
                    description = description ?: "",
                    price = price,
                    imageUrl = imageUrl ?: "URL NOT FOUND",
                    categoryId = categoryId,
                    currency = currency
                ),
                quantity = quantity.toInt()
            )
        }.asFlow().mapToList(Dispatchers.Default)
    }

    override fun getTotalPrice(): Flow<Long> {
        return cartItemQueries.selectCartTotal {
            it ?: 0L
        }.asFlow().mapToOne(Dispatchers.Default)
    }

    override suspend fun addToCart(productId: String, quantity: Long) {
        cartItemQueries.upsertCartItem(productId, quantity, Clock.System.now().toString())
        //sync with supabase here later
    }

    override suspend fun removeFromCart(productId: String) {
        cartItemQueries.removeCartItem(productId)
    }

    override suspend fun clearCart() {
        cartItemQueries.clearCart()
    }
}