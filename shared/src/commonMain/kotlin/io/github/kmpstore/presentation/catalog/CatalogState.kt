package io.github.kmpstore.presentation.catalog

import io.github.kmpstore.domain.model.Category

data class CatalogState(
    val isLoading: Boolean = false,
    val topics: List<Category> = emptyList(),
    val error: String? = null
)