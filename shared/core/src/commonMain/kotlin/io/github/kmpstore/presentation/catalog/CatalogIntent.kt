package io.github.kmpstore.presentation.catalog

sealed class CatalogIntent {
    data class OnQueryChange(val query: String) : CatalogIntent()
}