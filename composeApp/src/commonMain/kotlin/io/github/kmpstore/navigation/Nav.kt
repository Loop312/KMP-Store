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
import io.github.kmpstore.presentation.catalog.CategoryDetailScreen
import io.github.kmpstore.presentation.product.ProductScreen

@Composable
fun Nav() {
    val backStack = remember { mutableStateListOf<Route>(Route.ProductCatalog) }
    val onProductClick: (String) -> Unit = remember {
        { productId ->
            backStack.add(Route.ProductDetail(productId))
        }
    }
    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<Route.ProductCatalog> {
                CatalogScreen(
                    onCategoryClick = { id, name ->
                        backStack.add(Route.CategoryDetail(id, name))
                    },
                    onProductClick = onProductClick
                )
            }
            entry<Route.CategoryDetail> { route ->
                CategoryDetailScreen(
                    categoryId = route.categoryId,
                    categoryName = route.categoryName,
                    onProductClick = onProductClick
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
                        backStack.clear()
                        backStack.add(Route.ProductCatalog)
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