package io.github.kmpstore.di

import io.github.kmpstore.data.repository.SupabaseProductRepository
import io.github.kmpstore.domain.repository.ProductRepository
import io.github.kmpstore.presentation.catalog.CatalogViewModel
import io.github.kmpstore.presentation.product.ProductViewModel
import org.koin.dsl.module

val catalogModule = module {
    single<ProductRepository> { SupabaseProductRepository(get()) }

    factory<CatalogViewModel> { CatalogViewModel(get()) }
    factory { (productId: String) ->
        ProductViewModel(productId = productId, repository = get())
    }
}