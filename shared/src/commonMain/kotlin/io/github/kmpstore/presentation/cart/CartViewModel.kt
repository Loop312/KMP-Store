package io.github.kmpstore.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.kmpstore.domain.model.CartItem
import io.github.kmpstore.domain.repository.CartRepository
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class CartViewModel(
    private val repository: CartRepository,
    private val supabase: SupabaseClient
): ViewModel() {
    private val _state = MutableStateFlow(CartState())
    val state = _state.asStateFlow()

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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getCart()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { data ->
                    _state.update { it.copy(isLoading = false, items = data) }
                }
        }
    }

    private fun incrementItem(item: CartItem) {
        viewModelScope.launch {
            repository.addToCart(item.product.id, 1)
        }
    }

    private fun decrementItem(item: CartItem) {
        viewModelScope.launch {
            repository.addToCart(item.product.id, -1)
        }
    }

    private fun removeItem(item: CartItem) {
        viewModelScope.launch {
            repository.removeFromCart(item.product.id)
        }
    }

    private fun emptyCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    private fun checkout() {
        viewModelScope.launch {
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
                )
                println(response)
                println(response.bodyAsText())
                // of course will do it properly later
                val url = response.bodyAsText().drop(8).dropLast(2)
                println(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}