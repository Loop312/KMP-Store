package io.github.kmpstore.di

import coil3.PlatformContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

actual val platformNetworkModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<PlatformContext> { PlatformContext.INSTANCE }
}