package io.github.kmpstore.theme

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kmpstore.shared.ui.generated.resources.Res
import kmpstore.shared.ui.generated.resources.dark_mode
import kmpstore.shared.ui.generated.resources.light_mode
import org.jetbrains.compose.resources.painterResource

@Composable
fun ThemeToggleButton(modifier: Modifier = Modifier) {
        Icon(
            painter = painterResource(if (isDarkTheme) Res.drawable.light_mode else Res.drawable.dark_mode),
            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
            modifier = modifier.clickable{ isDarkTheme = !isDarkTheme }
        )
}