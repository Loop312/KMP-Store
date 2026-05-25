package io.github.kmpstore.data.remote.model

import io.github.kmpstore.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String,
    @SerialName("parent_id") val parentId: String?
) {
    constructor(category: Category) : this (
        id = category.id,
        name = category.name,
        slug = category.slug,
        parentId = category.parent_id
    )
}
