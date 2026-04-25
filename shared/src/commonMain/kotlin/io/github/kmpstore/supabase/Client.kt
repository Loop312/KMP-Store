package io.github.kmpstore.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.kmpstore.BuildKonfig

fun initSupabaseClient() = createSupabaseClient(
    supabaseUrl = BuildKonfig.SUPABASE_URL,
    supabaseKey = BuildKonfig.SUPABASE_KEY
) {
    install(Auth)
    install(Postgrest)
    //install other modules
}