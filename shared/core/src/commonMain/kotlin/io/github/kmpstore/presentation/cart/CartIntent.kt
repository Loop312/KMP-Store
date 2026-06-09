package io.github.kmpstore.presentation.cart

import io.github.kmpstore.domain.model.CartItem

sealed class CartIntent {
    data object Refresh : CartIntent()
    data class IncrementItem(val item: CartItem) : CartIntent()
    data class DecrementItem(val item: CartItem) : CartIntent()
    data class RemoveItem(val item: CartItem) : CartIntent()
    data object ClearCart : CartIntent()
    data object Checkout : CartIntent()
}