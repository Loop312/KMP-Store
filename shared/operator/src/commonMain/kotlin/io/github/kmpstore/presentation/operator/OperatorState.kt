package io.github.kmpstore.presentation.operator

import io.github.kmpstore.domain.model.ManifestItem
import io.github.kmpstore.domain.model.Order
import io.github.kmpstore.domain.model.Product

data class OperatorState(
    val currentTab: OperatorTab = OperatorTab.AVAILABLE,
    val availableOrders: List<Order> = emptyList(),
    val activeDeliveries: List<Order> = emptyList(),
    val historyOrders: List<Order> = emptyList(),
    val aggregateManifest: List<ManifestItem> = emptyList(),
    val isLoading: Boolean = false,
    val selectedOrderDetails: List<Pair<Product?, Int>> = emptyList(),
    val isDetailsLoading: Boolean = false,
    val error: String? = null
)

enum class OperatorTab { AVAILABLE, ACTIVE, HISTORY, MANIFEST }