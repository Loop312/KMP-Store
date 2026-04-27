package io.github.kmpstore.presentation.product

import io.github.kmpstore.domain.model.Product

data class ProductState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val error: String? = null
)
