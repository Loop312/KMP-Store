package io.github.kmpstore.di

import io.github.jan.supabase.SupabaseClient
import io.github.kmpstore.data.repository.SupabaseAuthRepository
import io.github.kmpstore.domain.repository.AuthRepository
import io.github.kmpstore.presentation.auth.LoginViewModel
import io.github.kmpstore.supabase.initSupabaseClient
import org.koin.dsl.module

val authModule = module {
    single<SupabaseClient> { initSupabaseClient() }
    single<AuthRepository> { SupabaseAuthRepository(supabaseClient = get()) }
    factory<LoginViewModel> { LoginViewModel(repository = get()) }
}