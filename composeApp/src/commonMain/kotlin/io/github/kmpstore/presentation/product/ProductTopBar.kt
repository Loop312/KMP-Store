package io.github.kmpstore.presentation.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.kmpstore.pad
import kmpstore.composeapp.generated.resources.Res
import kmpstore.composeapp.generated.resources.account_circle
import kmpstore.composeapp.generated.resources.add_to_cart
import kmpstore.composeapp.generated.resources.shopping_cart
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProductTopBar(
    productName: String,
    onAddToCart: () -> Unit,
    onCartClick: () -> Unit,
    onAccountClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(productName, overflow = TextOverflow.Ellipsis) },
        actions = {
            Icon(painterResource(Res.drawable.add_to_cart), "add_to_cart", Modifier.size(32.dp).clickable(onClick = onAddToCart))
            Spacer(Modifier.width(pad/2))
            Icon(painterResource(Res.drawable.shopping_cart), "shopping_cart", Modifier.size(32.dp).clickable(onClick = onCartClick))
            Spacer(Modifier.width(pad/2))
            Icon(painterResource(Res.drawable.account_circle), "account", Modifier.size(32.dp).clickable(onClick = onAccountClick))
            Spacer(Modifier.width(pad/2))
        }
    )
}