package io.github.kmpstore.presentation.catalog

import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.domain.model.Product

data class CatalogState(
    val isLoading: Boolean = false,
    val topics: List<Category> = emptyList(),
    val searchResults: List<Product> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)