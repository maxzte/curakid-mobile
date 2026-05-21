package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.database.AppDatabase
import com.misterio.core.domain.repository.AuthRepository
import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.dto.ForgotPasswordRequest
import com.misterio.core.network.dto.LoginRequest
import com.misterio.core.network.dto.OAuthRequest
import com.misterio.core.network.dto.RefreshTokenRequest
import com.misterio.core.network.dto.RegisterRequest
import com.misterio.core.network.dto.ResetPasswordRequest
import com.misterio.core.network.dto.VerifyEmailRequest
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.curakid.core.common.SettingsManager
import com.misterio.curakid.core.common.TokenManager
import kotlinx.coroutines.Dispatchers
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

    override suspend fun oAuthLogin(idToken: String): Result<Unit> {
        return try {
            val response = authApi.oAuthLogin(OAuthRequest(idToken))
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

    override suspend fun register(email: String, password: String, name: String): Result<String> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, name))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> {
        return try {
            authApi
            val response = authApi.verifyEmail(VerifyEmailRequest(email, code))
            if (response.isSuccessful && response.body() != null) {
                val tokens = response.body()!!
                appDataStore.setOnboardingCompleted(tokens.onboardingCompleted)
                tokenManager.save(tokens.accessToken, tokens.refreshToken)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Verification failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = authApi.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Forgot password failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): Result<String> {
        return try {
            val response = authApi.resetPassword(ResetPasswordRequest(email, code, newPassword))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Reset password failed: ${response.code()}"))
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
            val res = authApi.refresh(RefreshTokenRequest(tokenManager.currentRefreshToken!!))
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
            tokenManager.clearTokens()
            database.clearAllTables()
            settingsManager.clear()
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
