package io.github.kmpstore.presentation.product

sealed class ProductUiEffect {
    data class ShowSnackbar(val message: String) : ProductUiEffect()
}