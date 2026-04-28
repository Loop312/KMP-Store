package io.github.kmpstore.di

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import io.github.kmpstore.data.repository.SupabaseProductRepository
import io.github.kmpstore.domain.repository.ProductRepository
import io.github.kmpstore.presentation.catalog.CatalogViewModel
import io.github.kmpstore.presentation.product.ProductViewModel
import io.ktor.client.HttpClient
import org.koin.dsl.module

@OptIn(ExperimentalCoilApi::class)
val catalogModule = module {
    includes(networkModule)
    single<ProductRepository> { SupabaseProductRepository(get()) }
    single<ImageLoader> {
        ImageLoader.Builder(context = get()).components {
            add(SvgDecoder.Factory())
            add (
                KtorNetworkFetcherFactory(
                    httpClient = get<HttpClient>()
                )
            )
        }.build()
    }

    factory<CatalogViewModel> { CatalogViewModel(get()) }
    factory { (productId: String) ->
        ProductViewModel(productId = productId, repository = get())
    }
}