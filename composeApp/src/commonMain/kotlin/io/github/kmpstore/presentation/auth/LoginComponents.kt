package io.github.kmpstore.presentation.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.providers.OAuthProvider
import kmpstore.composeapp.generated.resources.Res
import kmpstore.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

@Composable
fun OAuthButton(provider: OAuthProvider, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.height(48.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.compose_multiplatform),
            contentDescription = provider.name,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(provider.name)
    }
}

@Composable
fun AuthDivider() {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f))
        Text(" Or continue with ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        HorizontalDivider(Modifier.weight(1f))
    }
}