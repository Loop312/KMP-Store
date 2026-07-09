package io.github.kmpstore.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.kmpstore.MAX_CART_SIZE
import io.github.kmpstore.domain.model.CartItem
import io.github.kmpstore.domain.repository.CartRepository
import io.github.kmpstore.domain.repository.ProductRepository
import io.ktor.client.call.body
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class CartViewModel(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val supabase: SupabaseClient
): ViewModel() {
    private val _state = MutableStateFlow(CartState())
    val state = _state.asStateFlow()

    val cartSize = _state.map { cartState ->
        cartState.items.sumOf { it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private val _checkoutUrl = MutableStateFlow<String?>(null)
    val checkoutUrl = _checkoutUrl.asStateFlow()

    private var loadCartJob: Job? = null

    private val _effects = Channel<CartUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        onIntent(CartIntent.Refresh)
    }

    fun onIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.Refresh -> loadCart()
            is CartIntent.IncrementItem -> incrementItem(intent.item)
            is CartIntent.DecrementItem -> decrementItem(intent.item)
            is CartIntent.RemoveItem -> removeItem(intent.item)
            is CartIntent.ClearCart -> emptyCart()
            is CartIntent.Checkout -> checkout()
        }
    }

    private fun loadCart() {
        loadCartJob?.cancel()
        loadCartJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            cartRepository.getCart()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { data ->
                    _state.update { it.copy(isLoading = false, items = data) }
                }
        }
    }

    private fun incrementItem(item: CartItem) {
        viewModelScope.launch {
            cartRepository.addToCart(item.product.id, 1)
        }
    }

    private fun decrementItem(item: CartItem) {
        viewModelScope.launch {
            cartRepository.addToCart(item.product.id, -1)
        }
    }

    private fun removeItem(item: CartItem) {
        viewModelScope.launch {
            cartRepository.removeFromCart(item.product.id)
        }
    }

    private fun emptyCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }

    private fun checkout() {
        viewModelScope.launch {
            val currentItems = _state.value.items

            val itemsToRemove = currentItems.filter { it.quantity <= 0 }
            val itemsToKeep = currentItems.filter { it.quantity > 0 }

            //remove items that are at 0
            itemsToRemove.forEach { item ->
                cartRepository.removeFromCart(item.product.id)
            }
            //ensure individual item count is valid
            if (itemsToKeep.isEmpty()) {
                _effects.send(CartUiEffect.ShowSnackbar("No items in cart"))
                return@launch
            }
            if (itemsToKeep.size > MAX_CART_SIZE) {
                _effects.send(CartUiEffect.ShowSnackbar("Too many individual items in cart (Max: ${MAX_CART_SIZE})"))
                return@launch
            }
            //make sure it doesn't surpass stock
            itemsToKeep.forEach {
                //update stock
                productRepository.refreshProduct(it.product.id)
                val stock = it.product.stock ?: return@forEach

                if (it.quantity > stock) {
                    _effects.send(CartUiEffect.ShowSnackbar("${it.product.name} has too many in cart. Stock: ${it.product.stock}, In Cart: ${it.quantity}"))
                    cartRepository.addToCart(it.product.id, stock - it.quantity)
                    return@launch
                }
            }
            try {
                val response = supabase.functions.invoke(
                    function = "stripe-checkout",
                    body = buildJsonObject {
                        putJsonArray("items") {
                            _state.value.items.forEach { item ->
                                addJsonObject {
                                    put("price", item.product.priceId)
                                    put("quantity", item.quantity)
                                }
                            }
                        }
                    }
                ).body<Checkout>()
                println(response.url)
                _effects.send(CartUiEffect.ShowSnackbar("Valid Request. Sending to checkout and emptying cart."))
                _checkoutUrl.value = response.url
                emptyCart()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Checkout failed: ${e.message}") }
            }
        }
    }
    fun clearCheckoutUrl() {
        _checkoutUrl.value = null
    }
}

@Serializable
data class Checkout(val url: String)