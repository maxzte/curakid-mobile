package com.misterio.core.network

import android.util.Log
import com.misterio.curakid.core.common.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.toString()
        Log.d(TAG, "intercept: START Request to $url")
        
        runBlocking {
            Log.d(TAG, "intercept: Waiting for TokenManager initialization for $url (timeout 5s)")
            withTimeoutOrNull(5000) {
                tokenManager.waitForInitialization()
            }
            Log.d(TAG, "intercept: TokenManager ready check finished for $url")
        }
        
        val token = tokenManager.currentToken
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            Log.d(TAG, "intercept: Adding token to request $url")
            requestBuilder.header("Authorization", "Bearer $token")
        } else {
            Log.w(TAG, "intercept: NO TOKEN for request $url")
        }
        
        return try {
            Log.d(TAG, "intercept: Proceeding with chain.proceed for $url")
            val response = chain.proceed(requestBuilder.build())
            Log.d(TAG, "intercept: Response received for $url: ${response.code}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "intercept: EXCEPTION for request $url", e)
            throw e
        }
    }

    private companion object {
        private const val TAG = "AuthInterceptor"
    }
}
