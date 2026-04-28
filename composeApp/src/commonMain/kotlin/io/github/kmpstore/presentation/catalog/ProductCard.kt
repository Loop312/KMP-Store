package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.kmpstore.domain.model.Product

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onProductClick(product.id) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // AsyncImage from Coil3 (KMP compatible)
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.height(120.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(8.dp)) {
                Text(product.name, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${product.currency} ${product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}