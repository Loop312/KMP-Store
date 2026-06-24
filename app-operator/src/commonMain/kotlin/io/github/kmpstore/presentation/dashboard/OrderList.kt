package io.github.kmpstore.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.Order
import io.github.kmpstore.util.scrollbarStyle
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter

@Composable
fun OrderList(
    orders: List<Order>,
    modifier: Modifier = Modifier,
    onOrderClick: (Order) -> Unit,
    onAction: ((Order) -> Unit)? = null
) {
    if (orders.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No orders found in this category.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Box(Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onAction = if (onAction != null) {
                            { onAction(order) }
                        } else null,
                        onOrderClick = { onOrderClick(order) }
                    )
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