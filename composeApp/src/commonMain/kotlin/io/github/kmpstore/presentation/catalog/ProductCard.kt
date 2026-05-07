package io.github.kmpstore.presentation.catalog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.kmpstore.IMAGE_LOADING_ERROR
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.roundedCornerShape

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // on hover animations
    val popScale by animateFloatAsState(
        targetValue = if (isHovered) 1.16f else 1f,
        label = "Card Pop Scale"
    )
    val textAlphaScale by animateFloatAsState(
        targetValue = if (isHovered) .12f else 1f,
        label = "Card Text Alpha"
    )

    Card(
        onClick = { onProductClick(product.id) },
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .graphicsLayer {
                scaleX = popScale
                scaleY = popScale
            }
            .border(
                if (isHovered) 2.dp else 1.dp,
                MaterialTheme.colorScheme.primary,
                roundedCornerShape
            ),
        interactionSource = interactionSource,
        shape = roundedCornerShape
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                onError = {
                    println(IMAGE_LOADING_ERROR(product.name, product.imageUrl, it.result.toString()))
                }
            )
            Column(
                modifier = Modifier.padding(12.dp).align(Alignment.BottomStart).alpha(textAlphaScale)
            ) {
                Text(
                    text = product.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.formattedPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}