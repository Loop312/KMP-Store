package io.github.kmpstore.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kmpstore.shared.ui.generated.resources.Res
import kmpstore.shared.ui.generated.resources.shopping_cart
import org.jetbrains.compose.resources.painterResource

@Composable
fun CartButton(
    cartSize: Int,
    onClick: () -> Unit
) {
    BadgedBox(
        badge = {
            // Only display the badge if there's at least 1 item
            if (cartSize > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(text = cartSize.toString())
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick) // Move click handler here for better UX targets
    ) {
        Icon(
            painter = painterResource(Res.drawable.shopping_cart),
            contentDescription = "shopping_cart",
            modifier = Modifier.size(32.dp)
        )
    }
}