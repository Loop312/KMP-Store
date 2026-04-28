package io.github.kmpstore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.kmpstore.presentation.auth.LoginScreen
import io.github.kmpstore.presentation.catalog.CatalogScreen
import io.github.kmpstore.presentation.product.ProductScreen

@Composable
fun Nav() {
    val backStack = remember { mutableStateListOf<Route>(Route.ProductCatalog) }
    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<Route.ProductCatalog> {
                CatalogScreen(
                    onProductClick = { id ->
                        backStack.add(Route.ProductDetail(id))
                    }
                )
            }
            entry<Route.ProductDetail> { route ->
                ProductScreen(
                    id = route.productId
                )
            }
            entry<Route.Login> {
                LoginScreen(
                    onAuthSuccess = {
                        println("Login success")
                    }
                )
            }
        },
        modifier = Modifier.onKeyEvent {
            if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) backStack.removeLastOrNull()
            true
        }
    )
}