package io.github.kmpstore.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _state = MutableStateFlow(CatalogState())
    val state = _state.asStateFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        // 1. Immediately start collecting cached local DB data in a separate coroutine
        viewModelScope.launch {
            repository.getStoreFront()
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { data ->
                    _state.update { it.copy(topics = data, isLoading = false) }
                }
        }

        // 2. Refresh products from Supabase in the background
        viewModelScope.launch {
            // Only show full loading spinner if there is no local DB data cached yet
            _state.update { it.copy(isLoading = it.topics.isEmpty()) }
            repository.refreshProducts()
            _state.update { it.copy(isLoading = false) }
        }
    }
}