package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.kmpstore.presentation.ErrorMessage
import io.github.kmpstore.presentation.cart.CartViewModel
import io.github.kmpstore.presentation.drawer.CategoryTreeDrawer
import io.github.kmpstore.presentation.drawer.DrawerIntent
import io.github.kmpstore.presentation.drawer.DrawerViewModel
import io.github.kmpstore.util.scrollbarStyle
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    catalogViewModel: CatalogViewModel = koinViewModel(),
    drawerViewModel: DrawerViewModel = koinViewModel(),
    cartViewModel: CartViewModel = koinViewModel(),
    onCategoryClick: (String, String) -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
) {
    val cartSize by cartViewModel.cartSize.collectAsState()
    CategoryTreeDrawer(
        onCategoryClick = { id, name -> onCategoryClick(id, name) },
        content = {
            CatalogScreenScaffold(
                viewModel = catalogViewModel,
                cartSize = cartSize,
                onDrawerClick = { drawerViewModel.onIntent(DrawerIntent.OpenDrawer) },
                onCategoryClick = onCategoryClick,
                onProductClick = onProductClick,
                onCartClick = onCartClick
            )
        }
    )
}
@Composable
private fun CatalogScreenScaffold(
    viewModel: CatalogViewModel = koinViewModel(),
    cartSize: Int,
    onDrawerClick: () -> Unit,
    onCategoryClick: (String, String) -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            CatalogTopBar(
                searchQuery = state.searchQuery,
                searchResults = state.searchResults,
                cartSize = cartSize,
                onCartClick = onCartClick,
                onProductClick = onProductClick,
                onCatalogIntent = viewModel::onIntent,
                onDrawerClick = onDrawerClick,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    ErrorMessage(
                        message = state.error!!,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    val listState = rememberLazyListState()
                    LazyColumn(modifier = Modifier.fillMaxSize(), listState) {
                        items(items = state.topics, key = { it.id }) { category ->
                            CategoryRow(category, onCategoryClick, onProductClick)
                        }
                    }
                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(listState),
                        style = scrollbarStyle(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}