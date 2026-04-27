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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getStoreFront()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { data ->
                    _state.update { it.copy(isLoading = false, topics = data) }
                }
        }
    }
}