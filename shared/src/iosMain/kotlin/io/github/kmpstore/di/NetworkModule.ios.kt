package io.github.kmpstore.di

import coil3.PlatformContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

actual val platformNetworkModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<PlatformContext> { PlatformContext.INSTANCE }
}