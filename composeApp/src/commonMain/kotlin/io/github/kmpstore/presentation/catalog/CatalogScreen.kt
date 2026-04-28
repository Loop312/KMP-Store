package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = koinViewModel(),
    onProductClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Box(Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            if (state.error != null) {
                Text(state.error!!)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(state.topics) { category ->
                    CategoryRow(category, onProductClick)
                }
            }
        }
    }
}