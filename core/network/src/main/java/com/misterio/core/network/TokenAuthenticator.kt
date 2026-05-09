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

        val refreshToken = runBlocking {
            tokenManager.refreshToken.firstOrNull()
        } ?: return null

        val authApi = authApiLazy.get()

        val newTokens = runBlocking {
            try {
                val refreshResponse = authApi.refresh(RefreshRequest(refreshToken))
                if (refreshResponse.isSuccessful) {
                    refreshResponse.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        return if (newTokens != null) {
            runBlocking {
                tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            }
            response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        } else {
            runBlocking {
                tokenManager.clearTokens()
            }
            null
        }
    }

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()
}
