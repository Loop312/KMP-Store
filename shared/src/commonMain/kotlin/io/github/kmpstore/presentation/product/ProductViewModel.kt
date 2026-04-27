package io.github.kmpstore.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel(
    val productId: String,
    val repository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state = _state.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getProductById(productId)
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { data ->
                    _state.update { it.copy(isLoading = false, product = data) }
                }
        }
    }
}