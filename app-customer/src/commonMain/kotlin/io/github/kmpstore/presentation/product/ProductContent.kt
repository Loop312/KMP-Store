package io.github.kmpstore.presentation.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.presentation.ProductImage

@Composable
internal fun ProductContent(product: Product, cartQuantity: Int, onIntent: (ProductIntent) -> Unit) {
    // Adaptive Layout Logic
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp
        val scrollState = rememberScrollState()

        if (isWideScreen) {
            // Landscape / Tablet / Desktop Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ProductImage(product, Modifier.fillMaxSize())
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    ProductDetails(product, cartQuantity, onIntent)
                }
            }
        } else {
            // Standard Mobile Portrait Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                ProductImage(product, Modifier.fillMaxWidth().height(400.dp))
                ProductDetails(product, cartQuantity, onIntent, Modifier.padding(20.dp))
            }
        }
    }
}