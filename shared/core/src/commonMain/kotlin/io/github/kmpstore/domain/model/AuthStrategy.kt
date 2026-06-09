package io.github.kmpstore.domain.model

import io.github.jan.supabase.auth.providers.OAuthProvider

sealed interface AuthStrategy {
    data class EmailPassword(val email: String, val password: String) : AuthStrategy
    data class MagicLink(val email: String) : AuthStrategy
    data class OAuth(val provider: OAuthProvider) : AuthStrategy
    data class OTP(val phone: String) : AuthStrategy
}