package io.github.kmpstore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Order(
    val id: String,
    @SerialName("customer_email") val customerEmail: String?,
    @SerialName("customer_name") val customerName: String?,
    @SerialName("total_amount") val totalAmount: Long,
    val currency: String,
    val items: Map<String?, Int?>,
    val status: DeliveryStatus,
    @SerialName("shipping_address") val shippingAddress: AddressDetails?,
    @SerialName("operator_id") val operatorId: String?,
    @SerialName("created_at") val createdAt: String
)