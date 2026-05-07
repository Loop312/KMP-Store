package io.github.kmpstore.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.kmpstore.BuildKonfig
import kotlinx.serialization.json.Json

fun initSupabaseClient() = createSupabaseClient(
    supabaseUrl = BuildKonfig.SUPABASE_URL,
    supabaseKey = BuildKonfig.SUPABASE_KEY
) {
    install(Auth)
    install(Postgrest)
    install(Functions)
    defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    //install other modules
}