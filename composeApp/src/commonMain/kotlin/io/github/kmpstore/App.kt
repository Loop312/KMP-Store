package io.github.kmpstore

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import io.github.kmpstore.di.authModule
import io.github.kmpstore.presentation.auth.LoginScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(koinConfiguration { modules(authModule) }) {
        LoginScreen {}
    }
}