package io.github.kmpstore.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryProductsDto(
    @SerialName("category_id") val categoryId: String,
    @SerialName("stripe_product_id") val productId: String,
)
