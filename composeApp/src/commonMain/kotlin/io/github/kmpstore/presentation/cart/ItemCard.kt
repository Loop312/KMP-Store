package io.github.kmpstore.presentation.cart

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.kmpstore.IMAGE_LOADING_ERROR
import io.github.kmpstore.domain.model.CartItem
import io.github.kmpstore.pad
import io.github.kmpstore.roundedCornerShape
import kmpstore.composeapp.generated.resources.Res
import kmpstore.composeapp.generated.resources.remove_shopping_cart

@Composable
fun ItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        onClick = { /* take to product page (will need to handle optimizing stack by cutting it)*/ },
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth()
            .padding(pad/2)
            .border(
        if (isHovered) 2.dp else 1.dp,
        MaterialTheme.colorScheme.primary,
        roundedCornerShape
        ),
        shape = roundedCornerShape,
        interactionSource = interactionSource,
    ) {
        Row {
            //Item Image
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                modifier = Modifier
                    .fillMaxHeight(),
                onError = {
                    println(IMAGE_LOADING_ERROR(item.product.name, item.product.imageUrl, it.result.toString()))
                }
            )
            //Item Details
            Column(modifier = Modifier.weight(2f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceAround) {
                Text(
                    text = item.product.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.product.description,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.product.currency} ${item.product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Visible
                )
                Text(
                    text = "Price: \$${item.quantity * item.product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    //color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Visible
                )
            }
            QuantitySelector(
                quantity = item.quantity,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                modifier = Modifier.align(Alignment.CenterVertically).weight(2f)
            )
            // Remove Button
            CustomIconButton(
                resource = Res.drawable.remove_shopping_cart,
                onClick = onRemove,
                modifier = Modifier.align(Alignment.Bottom).padding(pad/2),
                isDestructive = true
            )
        }
    }
}