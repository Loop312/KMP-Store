package io.github.kmpstore.presentation.cart

sealed class CartUiEffect {
    data class ShowSnackbar(val message: String) : CartUiEffect()
}