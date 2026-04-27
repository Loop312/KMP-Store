package io.github.kmpstore.presentation.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val state by viewModel.state.collectAsState()
    Box(Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            Text(state.product.toString()) //will change after adding navigation
        }
    }
}