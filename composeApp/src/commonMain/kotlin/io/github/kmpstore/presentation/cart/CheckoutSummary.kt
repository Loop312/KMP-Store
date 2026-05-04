package io.github.kmpstore.presentation.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.CartItem
import io.github.kmpstore.pad

@Composable
fun CheckoutSummary(items: List<CartItem>, onCheckout: () -> Unit) {
    val totalPrice = items.sumOf { it.product.price * it.quantity }
    val totalItems = items.sumOf { it.quantity }

    Column(
        modifier = Modifier.padding(pad),
        verticalArrangement = Arrangement.spacedBy(pad)
    ) {
        //price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Subtotal ($totalItems items)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = $$"$$$totalPrice", // Use currency from product if available[cite: 4, 6]
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Button(
            onClick = { onCheckout() },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Checkout")
        }
    }
}