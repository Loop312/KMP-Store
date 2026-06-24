package io.github.kmpstore.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.kmpstore.domain.model.DeliveryStatus
import io.github.kmpstore.domain.model.ManifestItem
import io.github.kmpstore.domain.model.Order
import io.github.kmpstore.domain.repository.OperatorRepository
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SupabaseOperatorRepository(
    private val supabase: SupabaseClient,
    private val productRepository: ProductRepository
) : OperatorRepository {
    private val table = supabase.from("operations", "orders")
    val operatorId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    //MAKE SURE RLS POLICIES ARE IN PLACE FOR operator_id = auth.uid/null
    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun observeAvailableOrders(): Flow<List<Order>> {
        return supabase.auth.sessionStatus.flatMapLatest { status ->
            val currentId = operatorId
            if (currentId == null) {
                flowOf(emptyList())
            } else {
                table.selectAsFlow(
                    primaryKey = Order::id,
                    filter = FilterOperation("status", FilterOperator.IN, listOf("pending", "processing"))
                ).map { orders ->
                    orders.filter { it.status == DeliveryStatus.PENDING }
                }
            }
        }
    }

    //MAKE SURE RLS POLICIES ARE IN PLACE FOR operator_id = auth.uid/null
    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun observeActiveDeliveries(): Flow<List<Order>> {
        return supabase.auth.sessionStatus.flatMapLatest { status ->
            val currentId = operatorId
            if (currentId == null) {
                flowOf(emptyList())
            } else {
                table.selectAsFlow(
                    primaryKey = Order::id,
                    filter = FilterOperation(
                        "status",
                        FilterOperator.IN,
                        DeliveryStatus.entries.map { it.name.lowercase() }
                    )
                )
            }.map { orders ->
                orders.filter { it.status == DeliveryStatus.PROCESSING || it.status == DeliveryStatus.SHIPPED }
            }
        }
    }

    //MAKE SURE RLS POLICIES ARE IN PLACE FOR operator_id = auth.uid/null
    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun observeOrderHistory(): Flow<List<Order>> {
        return supabase.auth.sessionStatus.flatMapLatest { status ->
            val currentId = operatorId
            if (currentId == null) {
                flowOf(emptyList())
            } else {
                table.selectAsFlow(
                    primaryKey = Order::id,
                    filter = FilterOperation(
                        "status",
                        FilterOperator.IN,
                        listOf(
                            DeliveryStatus.DELIVERED.name.lowercase(),
                            DeliveryStatus.CANCELLED.name.lowercase()
                        )
                    )
                )
            }
        }
    }

    override suspend fun claimOrder(orderId: String) {
        table.update({
            Order::operatorId setTo operatorId
            Order::status setTo DeliveryStatus.PROCESSING
        }) {
            filter {
                Order::id eq orderId
                Order::status eq DeliveryStatus.PENDING.name.lowercase()
            }
        }
    }

    override suspend fun dropOrder(orderId: String) {
        table.update({
            // Clear the operator assignment and reset the status
            Order::operatorId setTo null
            Order::status setTo DeliveryStatus.PENDING
        }) {
            filter {
                Order::id eq orderId
                // Guard rail: Ensure only the operator currently assigned can drop it
                Order::operatorId eq operatorId
                Order::status eq DeliveryStatus.PROCESSING.name.lowercase()
            }
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: DeliveryStatus) {
        table.update({
            Order::status setTo status
        }) {
            filter { Order::id eq orderId }
        }
    }

    /**
     * Aggregates all items across assigned active orders for packing list preparation.
     * Parses the Stripe Price metadata structure: {"price_id": "quantity"}
     */
    override suspend fun getAggregateManifest(): List<ManifestItem> {
        val activeOrders = table.select {
            filter {
                Order::operatorId eq operatorId
                or {
                    Order::status eq DeliveryStatus.PROCESSING.name.lowercase()
                    //Order::status eq DeliveryStatus.SHIPPED.name.lowercase()
                }
            }
        }.decodeList<Order>()

        val rawQuantities = mutableMapOf<String, Int>() // Key: Price ID, Value: Total Qty
        activeOrders.forEach { order ->
            runCatching {
                order.items.forEach { (key, value) ->
                    if (key != null) {
                        val qty = value ?: 0
                        rawQuantities[key] = (rawQuantities[key] ?: 0) + qty
                    }
                }
            }
        }

        val priceIds = rawQuantities.keys.toList()
        val products = runCatching {
            // Use .first() to grab the list from the Flow synchronously
            productRepository.getProductsByPriceIds(priceIds).first()
        }.getOrElse { exception ->
            println("Failed to fetch products: $exception")
            emptyList()
        }
        val productMap = products.associateBy { it.priceId }

        return rawQuantities.map { (priceId, totalQty) ->
            ManifestItem(priceId = priceId, quantity = totalQty, product = productMap[priceId])
        }
    }
}