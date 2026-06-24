package io.github.kmpstore.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.Order
import io.github.kmpstore.domain.model.Product
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsDialog(
    order: Order,
    detailedItems: List<Pair<Product?, Int>>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text(text = "Order Details #${order.id.take(8).uppercase()}")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Customer: ${order.customerName ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Email: ${order.customerEmail ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Date: ${order.createdAt.take(10)}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Shipping Details: \n${order.shippingAddress?.toFormattedString() ?: "No address provided"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(text = "Items Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false) // Prevents dialog sizing issues
                    ) {
                        items(detailedItems) { (product, quantity) ->
                            OrderDetailsProduct(product, quantity)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun OrderDetailsProduct(product: Product?, quantity: Int){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (product != null) {
            // Visual thumbnail for an easy packing workflow
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ID: ${product.priceId.take(12)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Fallback UI if product details couldn't be loaded
            Text(
                text = "Unknown Item",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        // Clean operator badge display for the quantity
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "Qty: $quantity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}