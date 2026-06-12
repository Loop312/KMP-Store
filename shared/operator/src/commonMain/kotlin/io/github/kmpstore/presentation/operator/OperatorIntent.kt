package io.github.kmpstore.presentation.operator

import io.github.kmpstore.domain.model.DeliveryStatus
import io.github.kmpstore.domain.model.Order

sealed interface OperatorIntent {
    data class ChangeTab(val tab: OperatorTab) : OperatorIntent
    data class ClaimOrder(val orderId: String) : OperatorIntent
    data class DropOrder(val orderId: String) : OperatorIntent
    data class ViewOrderDetails(val order: Order) : OperatorIntent
    object ClearOrderDetails : OperatorIntent
    data class UpdateStatus(val orderId: String, val nextStatus: DeliveryStatus) : OperatorIntent
    object LoadManifest : OperatorIntent
    data class ToggleManifestItemCollected(val priceId: String) : OperatorIntent
    object StartDelivery : OperatorIntent
    object DismissError : OperatorIntent
}