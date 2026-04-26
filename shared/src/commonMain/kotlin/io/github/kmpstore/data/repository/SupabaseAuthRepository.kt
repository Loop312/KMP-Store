package io.github.kmpstore.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.kmpstore.domain.model.AuthStrategy
import io.github.kmpstore.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// commonMain/.../data/repository/SupabaseAuthRepository.kt
class SupabaseAuthRepository(
    private val supabaseClient: SupabaseClient
) : AuthRepository {
    private val auth = supabaseClient.auth

    override suspend fun signIn(strategy: AuthStrategy): Result<Unit> = runCatching {
        when (strategy) {
            is AuthStrategy.EmailPassword -> {
                auth.signInWith(Email) {
                    email = strategy.email
                    password = strategy.password
                }
            }
            is AuthStrategy.MagicLink -> {
                auth.signInWith(Email) {
                    email = strategy.email
                    // supabase-kt handles magic links via email provider without password
                }
            }
            is AuthStrategy.OAuth -> {
                auth.signInWith(strategy.provider) {
                    // handle deep link here (needs to be handled in supabase dashboard and AndroidManifest/Info.plist)
                }
            }
            is AuthStrategy.OTP -> {
                auth.signInWith(Phone) { phone = strategy.phone }
            }
        }
    }

    override suspend fun signUp(strategy: AuthStrategy): Result<Unit> = runCatching {
        if (strategy is AuthStrategy.EmailPassword) {
            auth.signUpWith(Email) {
                email = strategy.email
                password = strategy.password
            }
        } else {
            throw UnsupportedOperationException("Signup only supported for Email/Password currently")
        }
    }

    override val sessionStatus: Flow<Boolean> = auth.sessionStatus
        .map { it is SessionStatus.Authenticated }

    override suspend fun signOut() {
        auth.signOut()
    }
}