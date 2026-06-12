package io.github.kmpstore.domain.repository

import io.github.kmpstore.domain.model.DeliveryStatus
import io.github.kmpstore.domain.model.ManifestItem
import io.github.kmpstore.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OperatorRepository {
    fun observeAvailableOrders(): Flow<List<Order>>
    fun observeActiveDeliveries(): Flow<List<Order>>
    fun observeOrderHistory(): Flow<List<Order>>

    suspend fun claimOrder(orderId: String)
    suspend fun dropOrder(orderId: String)
    suspend fun updateOrderStatus(orderId: String, status: DeliveryStatus)
    suspend fun getAggregateManifest(): List<ManifestItem>
}