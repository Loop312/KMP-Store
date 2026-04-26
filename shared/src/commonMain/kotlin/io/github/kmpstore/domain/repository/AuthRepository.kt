package io.github.kmpstore.domain.repository

import io.github.kmpstore.domain.model.AuthStrategy
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(strategy: AuthStrategy): Result<Unit>
    suspend fun signIn(strategy: AuthStrategy): Result<Unit>
    suspend fun signOut()
    val sessionStatus: Flow<Boolean>
}