package io.github.kmpstore.presentation.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.ManifestItem
import io.github.kmpstore.presentation.ProductImage
import io.github.kmpstore.presentation.operator.OperatorState
import io.github.kmpstore.roundedCornerShape
import io.github.kmpstore.util.scrollbarStyle
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
import kmpstore.app_operator.generated.resources.Res
import kmpstore.app_operator.generated.resources.check_mark
import org.jetbrains.compose.resources.painterResource

@Composable
fun ManifestScreen(
    state: OperatorState,
    onStartDelivery: () -> Unit,
    onToggleItemCollected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = onBack) { Text("Back to active deliveries") }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onStartDelivery) { Text("Start deliveries") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Consolidated Load Configuration List", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = state.aggregateManifest,
                        key = { item -> item.priceId }
                    ) { item ->
                        ManifestScreenItem(item, onToggleItemCollected)
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    style = scrollbarStyle(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun ManifestScreenItem(item: ManifestItem, toggleCollected: (String) -> Unit) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (item.isCollected) 0.50f else 1.0f
    )
    Card(
        modifier = Modifier.fillMaxWidth().alpha(cardAlpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val product = item.product
                if (product != null) {
                    ProductImage(
                        product = product,
                        modifier = Modifier
                            .height(160.dp)
                            .width(160.dp)
                            .clip(roundedCornerShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, roundedCornerShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Price ID: ${item.priceId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Product Details Not Found (Try Restarting App)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Price ID: ${item.priceId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Qty: ${item.quantity}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.isCollected) {
                    // Outlined button signals it's "Done" but can be undone
                    OutlinedButton(
                        onClick = { toggleCollected(item.priceId) }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.check_mark),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Collected")
                    }
                } else {
                    // Filled button draws attention to the pending task
                    Button(
                        onClick = { toggleCollected(item.priceId) }
                    ) {
                        Text("Collect")
                    }
                }
            }
        }
    }
}