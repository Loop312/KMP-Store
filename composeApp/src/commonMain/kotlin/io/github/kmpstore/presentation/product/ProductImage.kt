package io.github.kmpstore.presentation.product

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.kmpstore.IMAGE_LOADING_ERROR
import io.github.kmpstore.domain.model.Product

@Composable
internal fun ProductImage(product: Product, modifier: Modifier) {
    AsyncImage(
        model = product.imageUrl,
        contentDescription = product.name,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        onError = {
            println(IMAGE_LOADING_ERROR(product.name, product.imageUrl, it.result.toString()))
        }
    )
}