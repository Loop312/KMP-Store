package io.github.kmpstore.presentation.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmpstore.domain.model.DeliveryStatus
import io.github.kmpstore.domain.model.Order
import io.github.kmpstore.domain.repository.OperatorRepository
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

//MAKE SURE RLS POLICIES ARE IN PLACE FOR operator_id = auth.uid/null
class OperatorViewModel(
    private val repository: OperatorRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OperatorState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<OperatorState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.refreshProducts()
                .onSuccess { println("SUCCESSFULLY RETRIEVED PRODUCTS: $it") }
                .onFailure { error -> _state.update{ it.copy(error = error.message)} }
        }

        observeRepositoryOrders()
    }

    fun handleIntent(intent: OperatorIntent) {
        when (intent) {
            is OperatorIntent.ChangeTab -> _state.update { it.copy(currentTab = intent.tab) }
            is OperatorIntent.ClaimOrder -> claimOrder(intent.orderId)
            is OperatorIntent.DropOrder -> dropOrder(intent.orderId)
            is OperatorIntent.ViewOrderDetails -> fetchOrderProductDetails(intent.order)
            is OperatorIntent.ClearOrderDetails -> _state.update {
                it.copy(selectedOrderDetails = emptyList())
            }
            is OperatorIntent.UpdateStatus -> updateStatus(intent.orderId, intent.nextStatus)
            is OperatorIntent.LoadManifest -> fetchManifest()
            is OperatorIntent.ToggleManifestItemCollected -> toggleManifestItemCollected(intent.priceId)
            is OperatorIntent.StartDelivery -> startDelivery()
            is OperatorIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    //MAKE SURE RLS POLICIES ARE IN PLACE FOR operator_id = auth.uid/null
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRepositoryOrders() {
        _state.map { it.currentTab }
            .distinctUntilChanged()
            .flatMapLatest { tab ->
                when (tab) {
                    OperatorTab.AVAILABLE -> repository.observeAvailableOrders().onEach { orders ->
                        _state.update { it.copy(availableOrders = orders) }
                    }
                    OperatorTab.ACTIVE, OperatorTab.MANIFEST -> repository.observeActiveDeliveries().onEach { orders ->
                        _state.update { it.copy(activeDeliveries = orders) }
                    }
                    OperatorTab.HISTORY -> repository.observeOrderHistory().onEach { orders ->
                        _state.update { it.copy(historyOrders = orders) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun claimOrder(id: String) = viewModelScope.launch {
        runCatching { repository.claimOrder(id) }
            .onFailure { t -> _state.update { it.copy(error = t.message) } }
    }

    private fun dropOrder(id: String) = viewModelScope.launch {
        runCatching { repository.dropOrder(id) }
            .onFailure { t -> _state.update { it.copy(error = t.message) }; println(t.message) }
    }

    private fun updateStatus(id: String, status: DeliveryStatus) = viewModelScope.launch {
        runCatching { repository.updateOrderStatus(id, status) }
            .onFailure { t -> _state.update { it.copy(error = t.message) } }
    }

    private fun fetchOrderProductDetails(order: Order) = viewModelScope.launch {
        _state.update { it.copy(isDetailsLoading = true) }

        // Filter out null keys and extract priceIds
        val priceIds = order.items.keys.filterNotNull()

        runCatching {
            // Fetch products using your existing ProductRepository method
            productRepository.getProductsByPriceIds(priceIds).first()
        }.onSuccess { products ->
            val productMap = products.associateBy { it.priceId }

            // Construct a clean list of Products paired with their quantities
            val detailedItems = order.items.map { (priceId, quantity) ->
                val product = productMap[priceId]
                val qty = quantity ?: 0
                product to qty
            }

            _state.update { it.copy(selectedOrderDetails = detailedItems, isDetailsLoading = false) }
        }.onFailure { t ->
            _state.update {
                it.copy(
                    error = "Failed to load item info: ${t.message}",
                    isDetailsLoading = false
                )
            }
        }
    }

    private fun fetchManifest() = viewModelScope.launch {
        // OPTIMIZATION: If we already loaded the manifest, don't overwrite it!
        /* (Note: If we do this, we just need to make sure that when a delivery
        is fully completed or cleared, we set aggregateManifest = emptyList()
        so it knows to pull a fresh one for the next run!) */
//        if (_state.value.aggregateManifest.isNotEmpty()) {
//            return@launch
//        }
        _state.update { it.copy(isLoading = true) }
        runCatching { repository.getAggregateManifest() }
            .onSuccess { manifest -> _state.update { it.copy(aggregateManifest = manifest, isLoading = false) } }
            .onFailure { t -> _state.update { it.copy(error = t.message, isLoading = false) } }
    }

    private fun toggleManifestItemCollected(priceId: String) {
        _state.update { currentState ->
            val currentManifest = currentState.aggregateManifest
            val targetIndex = currentManifest.indexOfFirst { it.priceId == priceId }

            // If the item isn't found, return the current state unmodified
            if (targetIndex == -1) {
                currentState
            } else {
                val updatedManifest = currentManifest.toMutableList().apply {
                    this[targetIndex] = this[targetIndex].copy(isCollected = !this[targetIndex].isCollected)
                }
                currentState.copy(aggregateManifest = updatedManifest)
            }
        }
    }

    private fun startDelivery() = viewModelScope.launch {
        state.value.activeDeliveries.forEach { delivery ->
            if (delivery.status == DeliveryStatus.PROCESSING) updateStatus(delivery.id, DeliveryStatus.SHIPPED)
        }
    }
}