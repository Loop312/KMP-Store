package io.github.kmpstore.di

import coil3.PlatformContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.dsl.module

actual val platformNetworkModule = module {
    single<HttpClientEngine> { Js.create() }
    single<PlatformContext> { PlatformContext.INSTANCE }
}