package io.github.kmpstore.presentation.auth

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Google
import io.github.kmpstore.STORE_NAME
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // UI Local State
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(AuthMode.EMAIL) }
    var isSignUpMode by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onAuthSuccess()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(STORE_NAME, style = MaterialTheme.typography.headlineLarge)
            Text(
                text = if (isSignUpMode) "Create Account" else "Welcome Back",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(32.dp))

            // 1. Primary Auth Methods
            when (authMode) {
                AuthMode.EMAIL -> EmailPasswordContent(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    isSignUpMode = isSignUpMode,
                    onPrimaryAction = {
                        val intent = if (isSignUpMode) LoginIntent.SignUpWithEmail(email, password)
                        else LoginIntent.LoginWithEmail(email, password)
                        viewModel.handleIntent(intent)
                    },
                    onMagicLink = { viewModel.handleIntent(LoginIntent.LoginWithMagicLink(email)) }
                )

                AuthMode.PHONE -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        phone, { phone = it },
                        label = { Text("Phone (e.g. +1234567)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.handleIntent(LoginIntent.LoginWithOTP(phone)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Send SMS Code") }
                }
            }

            // 2. Mode Toggles
            TextButton(onClick = { authMode = if (authMode == AuthMode.EMAIL) AuthMode.PHONE else AuthMode.EMAIL }) {
                Text(if (authMode == AuthMode.EMAIL) "Use Phone instead" else "Use Email instead")
            }

            TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                Text(if (isSignUpMode) "Already have an account? Log in" else "Need an account? Sign up")
            }

            AuthDivider()

            // 3. Social Logins
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                listOf(Google, Github, Apple).forEach { provider ->
                    OAuthButton(provider) { viewModel.handleIntent(LoginIntent.LoginWithOAuth(provider)) }
                }
            }

            // 4. Feedback States
            if (state.isLoading) CircularProgressIndicator(Modifier.padding(top = 16.dp))
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}