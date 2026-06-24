package io.github.kmpstore

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.kmpstore.di.DriverFactory
import io.github.kmpstore.di.authModule
import io.github.kmpstore.di.catalogModule
import io.github.kmpstore.di.operatorModule
import io.github.kmpstore.di.sqldelightModule
import io.github.kmpstore.presentation.auth.LoginScreen
import io.github.kmpstore.presentation.dashboard.OperatorDashboardScreen
import io.github.kmpstore.shared.Database
import io.github.kmpstore.theme.AppTheme
import io.github.kmpstore.theme.isDarkTheme
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

@Composable
@Preview
fun App() {
    var isDbReady by remember { mutableStateOf(false) }
    isDarkTheme = isSystemInDarkTheme()
    KoinApplication(
        koinConfiguration {
            modules(
                authModule,
                catalogModule,
                sqldelightModule,
                operatorModule
            )
        }
    ) {
        val koin = getKoin()
        LaunchedEffect(Unit) {
            val driverFactory = koin.get<DriverFactory>()
            val driver = driverFactory.createDriver()
            val database = Database(driver)
            koin.loadModules(listOf(module {single<Database> { database }}))
            isDbReady = true
        }
        setSingletonImageLoaderFactory { koin.get<ImageLoader>() }

        AppTheme(isDarkTheme) {
            var isAuthenticated by remember { mutableStateOf(false) }
            val supabaseClient = koin.get<SupabaseClient>()
            val sessionStatus = supabaseClient.auth.sessionStatus.collectAsState()
            LaunchedEffect(sessionStatus.value) {
//                supabaseClient.auth.signOut()
                val currentUser = supabaseClient.auth.currentUserOrNull()
                if (currentUser != null) {
                    isAuthenticated = true
                }
            }
            if (isDbReady) {
                if (isAuthenticated) OperatorDashboardScreen() else LoginScreen(onAuthSuccess = { isAuthenticated = true })
            } else {
                Box(Modifier.fillMaxSize()) {
                    Text("Loading Cache...")
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}