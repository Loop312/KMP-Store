package io.github.kmpstore.domain.model

data class Category(
    val id: String,
    val title: String,
    val products: List<Product> = emptyList()
)
