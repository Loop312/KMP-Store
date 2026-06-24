package io.github.kmpstore.presentation.cart

import io.github.kmpstore.domain.model.CartItem

data class CartState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val error: String? = null
)