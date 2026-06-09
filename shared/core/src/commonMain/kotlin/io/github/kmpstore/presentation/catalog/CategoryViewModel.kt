package io.github.kmpstore.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmpstore.domain.model.Resource
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryId: String,
    private val repository: ProductRepository
) : ViewModel() {
    private val _products = MutableStateFlow<ProductRepository.productList>(Resource.Loading)
    val products = _products.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getProductsByCategory(categoryId).collect {
                _products.value = it
            }
        }
    }
}