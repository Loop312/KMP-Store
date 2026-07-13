package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.Resource
import io.github.kmpstore.pad
import io.github.kmpstore.presentation.ProductCard
import io.github.kmpstore.presentation.drawer.CategoryTreeDrawer
import io.github.kmpstore.presentation.drawer.DrawerIcon
import io.github.kmpstore.presentation.drawer.DrawerIntent
import io.github.kmpstore.presentation.drawer.DrawerViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun CategoryDetailScreen(
    categoryId: String,
    categoryName: String,
    categoryViewModel: CategoryViewModel = koinViewModel(key = categoryId) { parametersOf(categoryId) },
    drawerViewModel: DrawerViewModel = koinViewModel(),
    onProductClick: (String) -> Unit,
    onCategoryClick: (String, String) -> Unit,
) {
    CategoryTreeDrawer(
        viewModel = drawerViewModel,
        onCategoryClick = { id, name -> onCategoryClick(id, name) },
        content = {
            CategoryDetailScaffold(
                categoryId = categoryId,
                categoryName = categoryName,
                viewModel = categoryViewModel,
                onProductClick = onProductClick,
                onDrawerClick = { drawerViewModel.onIntent(DrawerIntent.OpenDrawer)}
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScaffold(
    categoryId: String,
    categoryName: String,
    viewModel: CategoryViewModel = koinViewModel(key = categoryId) { parametersOf(categoryId) },
    onProductClick: (String) -> Unit,
    onDrawerClick: () -> Unit
) {
    val productsResource by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { DrawerIcon(onClick = onDrawerClick) },
                title = { Text(categoryName) }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
            when (val resource = productsResource) {
                is Resource.Loading -> CircularProgressIndicator()
                is Resource.Success -> {
                    val products = resource.data
                    LazyVerticalGrid(
                        columns = GridCells.FixedSize(180.dp),
                        contentPadding = PaddingValues(pad/2),
                        horizontalArrangement = Arrangement.spacedBy(pad),
                        verticalArrangement = Arrangement.spacedBy(pad),
                        modifier = Modifier.padding(pad).fillMaxSize(),
                    ) {
                        items(items = products, key = { it.id }) { product ->
                            ProductCard(product, onProductClick)
                        }
                    }
                }
                is Resource.Error -> Text("Error: ${resource.message}")
                is Resource.NotFound -> Text("No products found in this category.")
            }
        }
    }
}