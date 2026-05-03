package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.pad
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    categoryName: String,
    viewModel: CategoryViewModel = koinViewModel(key = categoryId) { parametersOf(categoryId) },
    onProductClick: (String) -> Unit
) {
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(categoryName) }) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.FixedSize(160.dp),//.Adaptive(minSize = 180.dp),
            contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(pad),
            verticalArrangement = Arrangement.spacedBy(pad),
            modifier = Modifier.padding(pad).fillMaxSize(),
        ) {
            items(products) { product ->
                ProductCard(product, onProductClick)
            }
        }
    }
}