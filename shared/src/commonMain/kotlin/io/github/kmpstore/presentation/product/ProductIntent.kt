package io.github.kmpstore.presentation.product

sealed class ProductIntent {
    object IncrementCartCounter : ProductIntent()
    object DecrementCartCounter : ProductIntent()
}