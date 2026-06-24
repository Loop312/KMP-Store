package io.github.kmpstore.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.DeliveryStatus
import io.github.kmpstore.domain.model.Order

@Composable
fun OrderCard(
    order: Order,
    onAction: (() -> Unit)?,
    onOrderClick: () -> Unit
) {

    val totalItems = remember(order.items) {
        order.items.values.filterNotNull().sum()
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable{ onOrderClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.take(8).uppercase()}",
                    style = MaterialTheme.typography.titleMedium
                )
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Customer: ${order.customerName ?: "Anonymous"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Amount: ${(order.totalAmount / 100.0)} ${order.currency.uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Items: $totalItems",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom // Keeps the button in-line with the bottom text line
            ) {
                Text(
                    text = "Shipping Details: \n${order.shippingAddress?.toFormattedString() ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                // Dynamic action button based on context
                if (onAction != null) {
                    Button(
                        onClick = { onAction() },
                        colors = when (order.status) {
                            DeliveryStatus.PENDING -> { ButtonDefaults.buttonColors() }
                            DeliveryStatus.PROCESSING -> {
                                ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.error)
                            }
                            else -> {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    ) {
                        Text(
                            text = when (order.status) {
                                DeliveryStatus.PENDING -> "Claim Order"
                                DeliveryStatus.PROCESSING -> "Drop Order"
                                DeliveryStatus.SHIPPED -> "Mark as Delivered"
                                else -> "Update"
                            }
                        )
                    }
                }
            }
        }
    }
}