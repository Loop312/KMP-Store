package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.STORE_NAME
import io.github.kmpstore.pad
import kmpstore.composeapp.generated.resources.Res
import kmpstore.composeapp.generated.resources.account_circle
import kmpstore.composeapp.generated.resources.shopping_cart
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogTopBar(onCartClick: () -> Unit) {
    TopAppBar(
        title = { Text("$STORE_NAME's Catalog", style = MaterialTheme.typography.headlineMedium) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.padding(horizontal = pad),
        actions = {
            Icon(painterResource(Res.drawable.shopping_cart), "shopping_cart", Modifier
                    .size(32.dp)
                    .clickable(onClick = onCartClick)
            )
            Spacer(Modifier.width(pad/2))
            Icon(painterResource(Res.drawable.account_circle), "account", Modifier.size(32.dp))
        }
    )
}