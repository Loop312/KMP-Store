package io.github.kmpstore.di

import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule = module {
    includes(platformNetworkModule)
    single<HttpClient> {
        HttpClient(engine = get())
    }
}

expect val platformNetworkModule: Module