package com.misterio.core.network

import android.util.Log
import com.misterio.curakid.core.common.TokenManager
import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.dto.RefreshTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
    private val scope: CoroutineScope,
) : Authenticator {

    private val refreshLock = Any()
    private var refreshJob: Deferred<String?>? = null

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {
        Log.d(TAG, "authenticate: Triggered for ${response.request.url} with code ${response.code}")

        if (response.responseCount >= 2) {
            Log.w(TAG, "authenticate: Too many retries, giving up")
            return null
        }

        if (tokenManager.currentRefreshToken == null) return null

        val requestToken =
            response.request.header("Authorization")
                ?.removePrefix("Bearer ")

        val job = synchronized(refreshLock) {
            val currentToken = tokenManager.currentToken

            // Check if it was already refreshed by someone else
            if (
                !currentToken.isNullOrEmpty() &&
                currentToken != requestToken
            ) {
                Log.d(TAG, "authenticate: Token already refreshed by another thread")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // If there's an ongoing refresh, use it. Otherwise, start a new one.
            val existingJob = refreshJob
            if (existingJob == null) {
                Log.d(TAG, "authenticate: Starting new refresh job")
                val newJob = scope.async {
                    performRefresh()
                }
                refreshJob = newJob
                newJob
            } else {
                Log.d(TAG, "authenticate: Joining existing refresh job")
                existingJob
            }
        }

        val newToken = runBlocking {
            try {
                job.await()
            } catch (e: Exception) {
                Log.e(TAG, "authenticate: Refresh job failed", e)
                if (e is CancellationException) throw e
                null
            }
        }

        // Only clear the job if we are the ones who held this specific job
        synchronized(refreshLock) {
            if (refreshJob == job) {
                refreshJob = null
            }
        }

        return if (newToken != null) {
            Log.d(TAG, "authenticate: Refresh successful, retrying request")
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            Log.w(TAG, "authenticate: Refresh failed, returning null")
            null
        }
    }

    private suspend fun performRefresh(): String? {
        Log.d(TAG, "performRefresh: Starting")
        val refreshToken =
            tokenManager.currentRefreshToken
        
        if (refreshToken == null) {
            Log.w(TAG, "performRefresh: No refresh token available")
            return null
        }

        val refreshResponse = try {
            authApi.refresh(
                RefreshTokenRequest(refreshToken)
            )
        } catch (e: Exception) {
            Log.e(TAG, "performRefresh: Exception during API call", e)
            if (e is CancellationException) throw e
            null
        }

        if (
            refreshResponse == null ||
            !refreshResponse.isSuccessful
        ) {
            Log.w(TAG, "performRefresh: Refresh failed with code ${refreshResponse?.code()}")
            if (refreshResponse?.code() in listOf(400, 401, 403)) {
                Log.i(TAG, "performRefresh: Critical error, clearing tokens")
                tokenManager.clearTokens()
            }
            return null
        }

        val body = refreshResponse.body()
            ?: return null

        Log.i(TAG, "performRefresh: Tokens refreshed successfully")
        tokenManager.save(
            body.accessToken,
            body.refreshToken
        )

        return body.accessToken
    }

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }
            .count()

    private companion object {
        private const val TAG = "TokenAuthenticator"
    }
}
