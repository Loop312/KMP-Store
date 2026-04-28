package io.github.kmpstore.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Login : Route
    @Serializable
    data object ProductCatalog : Route
    @Serializable
    data class ProductDetail(val productId: String) : Route
}

