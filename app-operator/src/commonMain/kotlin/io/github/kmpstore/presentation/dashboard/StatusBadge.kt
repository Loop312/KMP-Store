package io.github.kmpstore.presentation.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.DeliveryStatus


@Composable
fun StatusBadge(status: DeliveryStatus) {
    val containerColor = when (status) {
        DeliveryStatus.PENDING -> MaterialTheme.colorScheme.errorContainer
        DeliveryStatus.PROCESSING -> MaterialTheme.colorScheme.primaryContainer
        DeliveryStatus.SHIPPED -> MaterialTheme.colorScheme.secondaryContainer
        DeliveryStatus.DELIVERED -> MaterialTheme.colorScheme.tertiaryContainer
        DeliveryStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceDim
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}