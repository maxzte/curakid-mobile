package com.misterio.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, name: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    fun isLoggedIn(): Flow<Boolean>
}
