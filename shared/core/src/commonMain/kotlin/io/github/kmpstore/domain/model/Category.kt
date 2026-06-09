package io.github.kmpstore.domain.model

data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val parentId: String?,
    val products: List<Product> = emptyList()
)
