package io.github.kmpstore.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmpstore.domain.model.AuthStrategy
import io.github.kmpstore.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = when (intent) {
                is LoginIntent.LoginWithEmail -> repository.signIn(AuthStrategy.EmailPassword(intent.email, intent.pass))
                is LoginIntent.SignUpWithEmail -> repository.signUp(AuthStrategy.EmailPassword(intent.email, intent.pass))
                is LoginIntent.LoginWithOAuth -> repository.signIn(AuthStrategy.OAuth(intent.provider))
                is LoginIntent.LoginWithMagicLink -> repository.signIn(AuthStrategy.MagicLink(intent.email))
                is LoginIntent.LoginWithOTP -> repository.signIn(AuthStrategy.OTP(intent.phone))
            }

            result.onSuccess {
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Authentication Failed") }
            }
        }
    }
}