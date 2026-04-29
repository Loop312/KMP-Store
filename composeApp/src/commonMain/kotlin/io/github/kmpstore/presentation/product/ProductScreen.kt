package io.github.kmpstore.presentation.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductScreen(
    id: String,
    viewModel: ProductViewModel = koinViewModel(key = id) { parametersOf(id) }
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            ProductTopBar(state.product)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.product != null -> {
                    ProductContent(product = state.product!!)
                }
                state.error != null -> {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}