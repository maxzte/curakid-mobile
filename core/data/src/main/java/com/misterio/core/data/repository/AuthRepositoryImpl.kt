package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.common.AppDataStore
import com.misterio.core.common.SettingsManager
import com.misterio.core.common.TokenManager
import com.misterio.core.database.AppDatabase
import com.misterio.core.domain.repository.AuthRepository
import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.dto.LoginRequest
import com.misterio.core.network.dto.RefreshRequest
import com.misterio.core.network.dto.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val database: AppDatabase,
    private val settingsManager: SettingsManager,
    private val appDataStore: AppDataStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val tokens = response.body()!!
                appDataStore.setOnboardingCompleted(tokens.onboardingCompleted)
                tokenManager.save(tokens.accessToken, tokens.refreshToken)
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
                appDataStore.setOnboardingCompleted(false)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        Log.i(TAG, "REFRESH")
        tokenManager.waitForInitialization()

        val hasRefreshToken = tokenManager.currentRefreshToken != null
        if (hasRefreshToken) {
            val res = authApi.refresh(RefreshRequest(tokenManager.currentRefreshToken!!))
            val body = res.body()
            if (res.isSuccessful && body != null) {
                tokenManager.save(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                )
            }
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i(TAG, "LOGOUT")
        try {
            val rToken = tokenManager.refreshToken.firstOrNull()
            tokenManager.clearTokens()
            database.clearAllTables()
            settingsManager.clear()

            rToken?.let {
                authApi.logout(RefreshRequest(it))
            }
            appDataStore.clear()

            Result.success(Unit)
        } catch (e: Exception) {
            database.clearAllTables()
            tokenManager.clearTokens()
            settingsManager.clear()
            appDataStore.clear()
            Result.failure(e)
        }
    }

    private companion object {
        private const val TAG = "AuthRepo"
    }
}
