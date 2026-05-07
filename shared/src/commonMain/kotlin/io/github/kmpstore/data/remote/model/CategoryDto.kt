package io.github.kmpstore.data.remote.model

import io.github.kmpstore.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?
) {
    constructor(category: Category) : this (
        id = category.id,
        name = category.name,
        slug = category.slug,
        description = category.description
    )
}
