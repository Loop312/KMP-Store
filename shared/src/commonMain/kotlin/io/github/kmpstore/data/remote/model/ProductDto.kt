package io.github.kmpstore.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    @SerialName("image_url")val imageUrl: String,
    @SerialName("category_id") val categoryId: String,
    val currency: String = "CAD"
) {
    constructor(product: io.github.kmpstore.Product) : this(
        id = product.id,
        name = product.name,
        description = product.description ?: "",
        price = product.price,
        imageUrl = product.image_url ?: "URL NOT FOUND",
        categoryId = product.category_id,
        currency = product.currency
    )
}
