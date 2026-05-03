package io.github.kmpstore.presentation.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.github.kmpstore.roundedCornerShape
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductScreen(
    id: String,
    onCartClick: () -> Unit,
    viewModel: ProductViewModel = koinViewModel(key = id) { parametersOf(id) }
) {
    val state by viewModel.state.collectAsState()

    // for telling user they've added it to cart
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProductUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
    Scaffold(
        topBar = {
            ProductTopBar(
                productName = state.product?.name ?: "Loading...",
                onAddToCart = { viewModel.addProductToCart(1) },
                onCartClick = onCartClick,
                onAccountClick = {},
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.clip(roundedCornerShape)) }
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
                    ProductContent(product = state.product!!, onAddToCart = { viewModel.addProductToCart(1) } )
                }
                state.error != null -> {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}