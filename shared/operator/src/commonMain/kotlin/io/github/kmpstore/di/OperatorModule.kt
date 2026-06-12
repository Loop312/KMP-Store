package io.github.kmpstore.di

import io.github.kmpstore.data.repository.SupabaseOperatorRepository
import io.github.kmpstore.domain.repository.OperatorRepository
import io.github.kmpstore.presentation.operator.OperatorViewModel
import org.koin.dsl.module

val operatorModule = module {
    single<OperatorRepository> { SupabaseOperatorRepository(get(), get()) }
    factory<OperatorViewModel> { OperatorViewModel(get(), get()) }
}