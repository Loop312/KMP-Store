package io.github.kmpstore.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmpstore.domain.repository.CartRepository
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        observeCartQuantity()
    }

    fun onIntent(intent: ProductIntent) {
        when (intent) {
            ProductIntent.IncrementCartCounter -> { addProductToCart(1) }
            ProductIntent.DecrementCartCounter -> { decrementProductInCart(1) }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            productRepository.getProductById(productId).collect { data ->
                _state.update { it.copy(product = data) }
            }
        }
    }

    fun addProductToCart(quantity: Long) {
        viewModelScope.launch {
            cartRepository.addToCart(productId, quantity)
            if (_state.value.cartQuantity == 0) {
                _effects.send(ProductUiEffect.ShowSnackbar("Added to cart"))
            }
        }
    }

    fun decrementProductInCart(quantity: Long) {
        viewModelScope.launch {
            if (_state.value.cartQuantity > 1) {
                cartRepository.addToCart(productId, -quantity)
            } else {
                cartRepository.removeFromCart(productId)
                _effects.send(ProductUiEffect.ShowSnackbar("Removed from cart"))
            }
        }
    }

    private fun observeCartQuantity() {
        viewModelScope.launch {
            cartRepository.getCart().collect { items ->
                val quantity = items.find { it.product.id == productId }?.quantity ?: 0
                _state.update { it.copy(cartQuantity = quantity) }
            }
        }
    }
}