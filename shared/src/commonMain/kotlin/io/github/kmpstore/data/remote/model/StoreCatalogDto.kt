package io.github.kmpstore.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreCatalogDto(
    @SerialName("product_id") val productId: String,
    val name: String,
    val description: String?,
    @SerialName("main_image") val mainImage: String?,
    @SerialName("category_name") val categoryName: String,
    @SerialName("category_slug") val categorySlug: String,
    @SerialName("unit_amount") val unitAmount: Long,
    val currency: String,
    @SerialName("price_id") val priceId: String,
    @SerialName("stock_level") val stock: Long,
)