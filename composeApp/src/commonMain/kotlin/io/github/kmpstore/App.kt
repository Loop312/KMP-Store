package io.github.kmpstore

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.kmpstore.di.DriverFactory
import io.github.kmpstore.di.authModule
import io.github.kmpstore.di.catalogModule
import io.github.kmpstore.di.sqldelightModule
import io.github.kmpstore.navigation.Nav
import io.github.kmpstore.shared.Database
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

@Composable
@Preview
fun App() {
    var isDbReady by remember { mutableStateOf(false) }
    KoinApplication(
        koinConfiguration {
            modules(
                authModule,
                catalogModule,
                sqldelightModule
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
        MaterialTheme {
            if (isDbReady) {
                Nav()
            } else Text(text = "Loading Cache...")
        }
    }
}