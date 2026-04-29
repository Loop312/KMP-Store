package io.github.kmpstore.presentation.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.pad
import kmpstore.composeapp.generated.resources.Res
import kmpstore.composeapp.generated.resources.account_circle
import kmpstore.composeapp.generated.resources.add_to_cart
import kmpstore.composeapp.generated.resources.shopping_cart
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTopBar(
    product: Product?
) {
    TopAppBar(
        title = {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(product?.name ?: "Loading...", overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(pad)) {
                    listOf(
                        painterResource(Res.drawable.add_to_cart),
                        painterResource(Res.drawable.shopping_cart),
                        painterResource(Res.drawable.account_circle)
                    ).forEach {
                        Icon(it, null, Modifier.size(pad*2))
                    }
                    Spacer(Modifier.width(pad))
                }
            }
        },
    )
}