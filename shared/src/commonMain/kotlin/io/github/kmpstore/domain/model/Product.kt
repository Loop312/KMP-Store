package io.github.kmpstore.domain.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Long,
    val imageUrl: String,
    val categoryId: String,
    val currency: String = "CAD"
) {
    constructor(product: io.github.kmpstore.Product) : this (
        id = product.id,
        name = product.name,
        description = product.description ?: "",
        price = product.price,
        imageUrl = product.image_url ?: "URL NOT FOUND",
        categoryId = product.category_id,
        currency = product.currency
    )
}