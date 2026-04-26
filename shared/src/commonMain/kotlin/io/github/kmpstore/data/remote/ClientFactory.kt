package io.github.kmpstore.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
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
    defaultSerializer = KotlinXSerializer(Json)
    //install other modules
}