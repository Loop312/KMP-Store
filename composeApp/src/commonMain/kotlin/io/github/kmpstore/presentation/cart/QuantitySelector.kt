package io.github.kmpstore.presentation.cart

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Row(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement Button
            QuantityControlNode(
                label = "−",
                onClick = { if (quantity > 0) onDecrement() },
                isDestructive = quantity <= 1
            )

            // Fixed-width container for the number to prevent jumping
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$quantity",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Increment Button
            QuantityControlNode(
                label = "+",
                onClick = onIncrement
            )
        }
    }
}

@Composable
private fun QuantityControlNode(
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Logic: Standard primary color, unless hovered, or if it's the minus button at 0/1
    val color = when {
        isDestructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .border(
                width = if (isHovered) 2.dp else 1.dp, // Only show border on hover
                color = color,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            // Centering adjustment for font-specific baselines
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}