package io.github.kmpstore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    @SerialName("image_url")val imageUrl: String,
    @SerialName("category_id") val categoryId: String,
    val currency: String = "CAD"
)