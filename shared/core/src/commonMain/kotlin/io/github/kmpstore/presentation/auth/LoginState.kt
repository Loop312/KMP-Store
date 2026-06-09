package io.github.kmpstore.presentation.auth

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)