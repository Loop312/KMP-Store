package io.github.kmpstore.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String?,
    @SerialName("main_image") val mainImage: String?,
    val price: Long,
    val currency: String = "CAD",
    @SerialName("price_id") val priceId: String,
    val stock: Long? = null,
)
