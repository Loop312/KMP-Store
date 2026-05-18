package io.github.kmpstore.presentation.product

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kmpstore.composeapp.generated.resources.Res
import kmpstore.composeapp.generated.resources.add_to_cart
import kmpstore.composeapp.generated.resources.remove_shopping_cart
import org.jetbrains.compose.resources.painterResource

@Composable
fun CartQuantityButton(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    // AnimatedContent provides a nice "pop" effect when switching modes
    AnimatedContent(
        targetState = quantity > 0,
        transitionSpec = {
            if (targetState) {
                (slideInVertically { height -> height } + fadeIn())
                    .togetherWith((slideOutVertically { height -> -height } + fadeOut()))
            } else {
                (slideInVertically { height -> -height } + fadeIn())
                    .togetherWith((slideOutVertically { height -> height } + fadeOut()))
            }.using(
                SizeTransform(clip = false)
            )
        },
        label = "CartButtonTransition"
    ) { isInCart ->
        if (isInCart) {
            // The Counter State: [-] [Qty] [+]
            Surface(
                modifier = modifier,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDecrease) {
                        Icon(
                            painter = painterResource(Res.drawable.remove_shopping_cart),
                            contentDescription = "Decrease quantity",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    IconButton(onClick = onIncrease) {
                        Icon(
                            painter = painterResource(Res.drawable.add_to_cart),
                            contentDescription = "Increase quantity",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        } else {
            // The Initial State: "Add to Cart"
            Button(
                onClick = onIncrease,
                modifier = modifier,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(painterResource(Res.drawable.add_to_cart), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add to Cart")
            }
        }
    }
}