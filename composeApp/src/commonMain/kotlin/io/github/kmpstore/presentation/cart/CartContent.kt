package io.github.kmpstore.presentation.cart

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.CartItem
import io.github.kmpstore.pad

@Composable
fun CartContent(
    items: List<CartItem>,
    onIntent: (CartIntent) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
            ) {
                LazyColumn(modifier = Modifier.weight(1.5f)) {
                    items(items) { item ->
                        ItemCard(
                            item = item,
                            onIncrement = { onIntent(CartIntent.IncrementItem(item)) },
                            onDecrement = { onIntent(CartIntent.DecrementItem(item)) },
                            onRemove = { onIntent(CartIntent.RemoveItem(item)) }
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CheckoutSummary(items) { onIntent(CartIntent.Checkout) }
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items) { item ->
                        ItemCard(
                            item = item,
                            onIncrement = { onIntent(CartIntent.IncrementItem(item)) },
                            onDecrement = { onIntent(CartIntent.DecrementItem(item)) },
                            onRemove = { onIntent(CartIntent.RemoveItem(item)) }
                        )
                    }
                }
                CheckoutSummary(items) { onIntent(CartIntent.Checkout) }
            }
        }
    }
}