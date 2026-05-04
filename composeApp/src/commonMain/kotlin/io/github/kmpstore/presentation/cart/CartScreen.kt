package io.github.kmpstore.presentation.cart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.kmpstore.presentation.ErrorMessage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CartScreen(
    viewModel: CartViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { CartTopBar() }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> ErrorMessage(state.error!!, Modifier.align(Alignment.Center))
                else -> CartContent(state.items, viewModel::onIntent)
            }
        }
    }
}