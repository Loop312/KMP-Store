package io.github.kmpstore.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmpstore.domain.repository.CartRepository
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel(
    val productId: String,
    val productRepository: ProductRepository,
    val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state = _state.asStateFlow()

    private val _effects = Channel<ProductUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            productRepository.getProductById(productId)
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { data ->
                    _state.update { it.copy(isLoading = false, product = data) }
                }
        }
    }

    fun addProductToCart(quantity: Long) {
        viewModelScope.launch {
            cartRepository.addToCart(productId, quantity)
            _effects.send(ProductUiEffect.ShowSnackbar("Added $quantity items to cart"))
        }
    }
}