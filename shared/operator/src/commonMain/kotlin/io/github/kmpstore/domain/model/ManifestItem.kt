package io.github.kmpstore.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ManifestItem(
    val priceId: String,
    val quantity: Int,
    @Transient val product: Product? = null,
    @Transient val isCollected: Boolean = false
)