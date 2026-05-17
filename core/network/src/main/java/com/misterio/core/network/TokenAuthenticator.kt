package com.misterio.core.network

import com.misterio.core.common.TokenManager
import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.dto.RefreshRequest
import dagger.Lazy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiLazy: Lazy<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if refresh token itself returns 401
        if (response.responseCount > 2) {
            return null
        }

        synchronized(this) {
            val currentAccessToken = runBlocking {
                tokenManager.accessToken.firstOrNull()
            }
            val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // 1. Check if token was already refreshed while this request was waiting for the lock
            if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            // 2. Perform refresh
            val refreshToken = runBlocking {
                tokenManager.refreshToken.firstOrNull()
            } ?: return null

            val authApi = authApiLazy.get()

            val refreshResponse = runBlocking {
                try {
                    authApi.refresh(RefreshRequest(refreshToken))
                } catch (e: Exception) {
                    null
                }
            }

            return if (refreshResponse != null && refreshResponse.isSuccessful) {
                val newTokens = refreshResponse.body()
                if (newTokens != null) {
                    runBlocking {
                        tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                    }
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                } else {
                    null
                }
            } else {
                // Only clear tokens if the refresh response indicates an invalid or expired refresh token
                // (e.g., 400 Bad Request, 401 Unauthorized, 403 Forbidden)
                if (refreshResponse != null && refreshResponse.code() in listOf(400, 401, 403)) {
                    runBlocking {
                        tokenManager.clearTokens()
                    }
                }
                null
            }
        }
    }

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()
}
