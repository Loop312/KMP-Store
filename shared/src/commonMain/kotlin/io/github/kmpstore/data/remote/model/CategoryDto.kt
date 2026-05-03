package io.github.kmpstore.data.remote.model

import io.github.kmpstore.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val title: String,
    val products: List<ProductDto> = emptyList()
) {
    constructor(category: Category) : this (
        id = category.id,
        title = category.title,
    )
}
