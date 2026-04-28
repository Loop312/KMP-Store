package io.github.kmpstore.presentation.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.kmpstore.IMAGE_LOADING_ERROR
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductScreen(
    id: String,
    viewModel: ProductViewModel = koinViewModel(key = id) { parametersOf(id) }
) {
    val state by viewModel.state.collectAsState()
    Box(Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            Row {
                AsyncImage(
                    model = state.product?.imageUrl,
                    contentDescription = state.product?.name,
                    modifier = Modifier.size(200.dp),
                    onError = {
                        println(
                            IMAGE_LOADING_ERROR(
                                state.product?.name,
                                state.product?.imageUrl,
                                it.result.toString()
                            )
                        )
                    }
                )
                Text(state.product.toString()) //will change after adding navigation
            }
        }
    }
}