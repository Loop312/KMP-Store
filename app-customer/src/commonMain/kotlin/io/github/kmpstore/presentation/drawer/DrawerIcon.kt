package io.github.kmpstore.presentation.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kmpstore.shared.ui.generated.resources.Res
import kmpstore.shared.ui.generated.resources.menu
import org.jetbrains.compose.resources.painterResource

@Composable
fun DrawerIcon(onClick: () -> Unit) {
    Icon(
        painter = painterResource(Res.drawable.menu),
        contentDescription = "Open navigation menu",
        modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onClick)
    )
}