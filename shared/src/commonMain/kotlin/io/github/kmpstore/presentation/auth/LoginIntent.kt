package io.github.kmpstore.presentation.auth

import io.github.jan.supabase.auth.providers.OAuthProvider

sealed class LoginIntent {
    data class LoginWithEmail(val email: String, val pass: String) : LoginIntent()
    data class SignUpWithEmail(val email: String, val pass: String) : LoginIntent()
    data class LoginWithOAuth(val provider: OAuthProvider) : LoginIntent()
    data class LoginWithMagicLink(val email: String) : LoginIntent()
    data class LoginWithOTP(val phone: String) : LoginIntent()
}