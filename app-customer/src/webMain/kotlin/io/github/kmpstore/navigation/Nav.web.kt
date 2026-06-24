package io.github.kmpstore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.terrakok.navigation3.browser.ChronologicalBrowserNavigation
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentParameters

@Composable
actual fun BindBrowserNavigation(backStack: SnapshotStateList<Route>) {
    ChronologicalBrowserNavigation(
        backStack = backStack,
        saveKey = { route ->
            when (route) {
                is Route.Login -> buildBrowserHistoryFragment("login")
                is Route.ProductCatalog -> buildBrowserHistoryFragment("catalog")
                is Route.ProductDetail -> buildBrowserHistoryFragment("product", mapOf("id" to route.productId))
                is Route.CategoryDetail -> buildBrowserHistoryFragment("category", mapOf("id" to route.categoryId, "name" to route.categoryName))
                is Route.Cart -> buildBrowserHistoryFragment("cart")
            }
        },
        restoreKey = { fragment ->
            val name = getBrowserHistoryFragmentName(fragment)
            val params = getBrowserHistoryFragmentParameters(fragment)
            when (name) {
                "login" -> Route.Login
                "catalog" -> Route.ProductCatalog
                "product" -> Route.ProductDetail(params["id"] ?: "")
                "category" -> Route.CategoryDetail(params["id"] ?: "", params["name"] ?: "")
                "cart" -> Route.Cart
                else -> null
            }
        }
    )
}