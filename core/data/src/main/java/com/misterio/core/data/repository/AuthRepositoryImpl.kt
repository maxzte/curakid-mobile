package com.misterio.core.data.repository

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.misterio.core.data.BuildConfig
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.AppDatabase
import com.misterio.core.domain.model.AuthState
import com.misterio.core.domain.model.ConsentType
import com.misterio.core.domain.repository.AuthRepository
import com.misterio.core.domain.repository.LegalDocumentRepository
import com.misterio.core.domain.repository.NotificationRepository
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.core.domain.util.suspendRunCatching
import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.api.UserApi
import com.misterio.core.network.dto.ConsentRequest
import com.misterio.core.network.dto.ForgotPasswordRequest
import com.misterio.core.network.dto.LoginRequest
import com.misterio.core.network.dto.OAuthRequest
import com.misterio.core.network.dto.RefreshTokenRequest
import com.misterio.core.network.dto.RegisterRequest
import com.misterio.core.network.dto.ResetPasswordRequest
import com.misterio.core.network.dto.VerifyEmailRequest
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.curakid.core.common.SecurityManager
import com.misterio.curakid.core.common.SettingsManager
import com.misterio.curakid.core.common.TokenManager
import com.misterio.curakid.core.common.TokenState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val database: AppDatabase,
    private val settingsManager: SettingsManager,
    private val securityManager: SecurityManager,
    private val appDataStore: AppDataStore,
    private val legalDocumentRepository: LegalDocumentRepository,
    private val notificationRepository: NotificationRepository,
    private val eventNotificationScheduler: EventNotificationScheduler,
) : AuthRepository {

    override fun isPasscodeEnabled(): Flow<Boolean> = securityManager.isPasscodeEnabled

    override fun getAuthState(): Flow<AuthState> {
        return combine(
            tokenManager.tokenState,
            appDataStore.onboardingCompleted
        ) { tokenState, onboardingCompleted ->
            when (tokenState) {
                is TokenState.Loading -> AuthState.LOADING
                is TokenState.Empty -> AuthState.UNAUTHENTICATED
                is TokenState.Available -> if (onboardingCompleted) AuthState.AUTHENTICATED else AuthState.NEEDS_ONBOARDING
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return safeApiCall { authApi.login(LoginRequest(email, password)) }.map { tokens ->
            appDataStore.setOnboardingCompleted(tokens.onboardingCompleted)
            tokenManager.save(tokens.accessToken, tokens.refreshToken)
        }
    }

    override suspend fun oAuthLogin(idToken: String): Result<Unit> {
        return safeApiCall { authApi.oAuthLogin(OAuthRequest(idToken)) }.map { tokens ->
            appDataStore.setOnboardingCompleted(tokens.onboardingCompleted)
            tokenManager.save(tokens.accessToken, tokens.refreshToken)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        isAccepted: Boolean
    ): Result<String> {
        val legalDocs = legalDocumentRepository.getLegalDocuments().getOrNull()
        val termsDoc = legalDocs?.find { it.type == ConsentType.TERMS }
        
        return safeApiCall {
            authApi.register(
                request = RegisterRequest(
                    email, password, name, consent = ConsentRequest(
                        termsAccepted = isAccepted,
                        privacyAccepted = isAccepted,
                        consentTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        consentVersion = termsDoc?.version ?: "1.0.0",
                        appVersion = BuildConfig.VERSION_NAME,
                        deviceLocale = Locale.getDefault().toLanguageTag()
                    )
                )
            )
        }.map { body ->
            body.message
        }
    }

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> {
        return safeApiCall { authApi.verifyEmail(VerifyEmailRequest(email, code)) }.map { tokens ->
            appDataStore.setOnboardingCompleted(tokens.onboardingCompleted)
            tokenManager.save(tokens.accessToken, tokens.refreshToken)
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return safeApiCall { authApi.forgotPassword(ForgotPasswordRequest(email)) }.map { body ->
            body.message
        }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): Result<String> {
        return safeApiCall { authApi.resetPassword(ResetPasswordRequest(email, code, newPassword)) }.map { body ->
            body.message
        }
    }

    override suspend fun refresh(): Unit = withContext(Dispatchers.IO) {
        Log.i(TAG, "REFRESH")
        suspendRunCatching {
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
        }.onFailure { e ->
            Log.e(TAG, "Refresh failed", e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        Log.i(TAG, "LOGOUT")
        val currentRefreshToken = tokenManager.currentRefreshToken
        if (!currentRefreshToken.isNullOrEmpty()) {
            safeApiCall { authApi.logout(RefreshTokenRequest(currentRefreshToken)) }
        }
        val fcmToken = appDataStore.fcmToken.first()
        if (!fcmToken.isNullOrBlank()) {
            notificationRepository.removeDeviceToken(fcmToken)
        }
        suspendRunCatching {
            clearSession()
        }
    }

    override suspend fun deleteAccount(): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        Log.i(TAG, "DELETE ACCOUNT")
        val fcmToken = appDataStore.fcmToken.first()
        if (!fcmToken.isNullOrBlank()) {
            notificationRepository.removeDeviceToken(fcmToken)
        }
        val result = safeApiCall { userApi.deleteCurrentUser() }
        clearSession()
        result.map { }
    }

    override suspend fun clearSession() {
        suspendRunCatching {
            eventNotificationScheduler.cancelAllEvents()
        }
        WorkManager.getInstance(context).cancelAllWork()
        database.clearAllTables()
        settingsManager.clear()
        securityManager.clearPasscode()
        appDataStore.clear()
        tokenManager.clearTokens()
    }

    private companion object {
        private const val TAG = "AuthRepo"
    }
}
