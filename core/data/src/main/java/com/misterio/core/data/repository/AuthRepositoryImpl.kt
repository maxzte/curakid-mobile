package com.misterio.core.data.repository

import com.misterio.core.common.SettingsManager
import com.misterio.core.common.TokenManager
import com.misterio.core.database.AppDatabase
import com.misterio.core.domain.repository.AuthRepository
import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.dto.LoginRequest
import com.misterio.core.network.dto.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val database: AppDatabase,
    private val settingsManager: SettingsManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val tokens = response.body()!!
                tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<Unit> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, name))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            authApi.logout()
            database.clearAllTables()
            settingsManager.clear()
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            database.clearAllTables() // Still clear local data
            settingsManager.clear()
            tokenManager.clearTokens() // Still clear tokens locally
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.accessToken.map { it != null }
    }
}
