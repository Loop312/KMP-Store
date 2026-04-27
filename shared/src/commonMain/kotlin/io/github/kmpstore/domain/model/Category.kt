package io.github.kmpstore.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val title: String,
    val products: List<Product> = emptyList()
)
