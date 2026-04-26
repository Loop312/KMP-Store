package io.github.kmpstore.presentation.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.kmpstore.STORE_NAME
import kmpstore.composeapp.generated.resources.Res
import kmpstore.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

// presentation/auth/LoginScreen.kt

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(), // Injected via Koin
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(AuthMode.EMAIL) }

    // Navigation trigger on success
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onAuthSuccess()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(STORE_NAME, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(32.dp))

            // 1. Dynamic Input Section
            when (authMode) {
                AuthMode.EMAIL -> {
                    OutlinedTextField(
                        email,
                        { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        password,
                        { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.handleIntent(LoginIntent.LoginWithEmail(email, password)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("Sign In") }

                    TextButton(onClick = { viewModel.handleIntent(LoginIntent.LoginWithMagicLink(email)) }) {
                        Text("Send Magic Link")
                    }
                }

                AuthMode.PHONE -> {
                    OutlinedTextField(
                        phone,
                        { phone = it },
                        label = { Text("Phone (e.g. +1234567)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.handleIntent(LoginIntent.LoginWithOTP(phone)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Send SMS Code") }
                }
            }

            // 2. Auth Mode Switcher
            TextButton(onClick = { authMode = if (authMode == AuthMode.EMAIL) AuthMode.PHONE else AuthMode.EMAIL }) {
                Text(if (authMode == AuthMode.EMAIL) "Use Phone instead" else "Use Email instead")
            }

            HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 1.dp, color = DividerDefaults.color)

            // 3. Modular Social Logins (OAuth)
            Text("Or continue with", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
                OAuthButton(Google) { viewModel.handleIntent(LoginIntent.LoginWithOAuth(Google)) }
                OAuthButton(Github) { viewModel.handleIntent(LoginIntent.LoginWithOAuth(Github)) }
                OAuthButton(Apple) { viewModel.handleIntent(LoginIntent.LoginWithOAuth(Apple)) }
            }

            if (state.isLoading) CircularProgressIndicator(Modifier.padding(top = 16.dp))
            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OAuthButton(provider: OAuthProvider, onClick: () -> Unit) {
    // In a white-label app, you'd use a dynamic icon based on the provider
    IconButton(onClick = onClick, modifier = Modifier.border(1.dp, Color.LightGray, CircleShape)) {
        Icon(painter = painterResource(Res.drawable.compose_multiplatform), contentDescription = provider.name)
    }
}

enum class AuthMode { EMAIL, PHONE }