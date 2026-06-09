package io.github.kmpstore

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.kmpstore.di.DriverFactory
import io.github.kmpstore.di.authModule
import io.github.kmpstore.di.catalogModule
import io.github.kmpstore.di.sqldelightModule
import io.github.kmpstore.navigation.Nav
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
        AppTheme(isDarkTheme) {
            var verification by remember { mutableStateOf(VERIFICATION) }
            if (verification) {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier
                        .align(Alignment.Center)
                        .border(1.dp, MaterialTheme.colorScheme.primary, roundedCornerShape),
                        Arrangement.SpaceAround,
                        Alignment.CenterHorizontally

                    ) {
                        Spacer(Modifier.height(pad))
                        Row {
                            Spacer(Modifier.width(pad))
                            Text(VERIFICATION_MESSAGE)
                            Spacer(Modifier.width(pad))
                        }
                        Spacer(Modifier.height(pad))
                        Row {
                            Button({ verification = false }) { Text("Yes") }
                            Spacer(Modifier.width(pad))
                            Button({}) { Text("No") }
                        }
                        Spacer(Modifier.height(pad))
                    }
                }
            } else {
                if (isDbReady) {
                    Nav()
                } else Text(text = "Loading Cache...")
            }
        }
    }
}