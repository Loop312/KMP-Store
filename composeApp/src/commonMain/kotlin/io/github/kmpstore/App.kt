package io.github.kmpstore

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import io.github.kmpstore.di.authModule
import io.github.kmpstore.di.catalogModule
import io.github.kmpstore.navigation.Nav
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(koinConfiguration { modules(authModule, catalogModule) }) {
        MaterialTheme {
            Nav()
        }
    }
}