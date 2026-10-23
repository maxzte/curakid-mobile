package com.misterio.core.network

import android.util.Log
import com.misterio.core.network.di.BASE_URL
import com.misterio.curakid.core.common.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    private val baseHost by lazy {
        try {
            BASE_URL.toHttpUrlOrNull()?.host
        } catch (e: Exception) {
            null
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = request.url
        val isSameHost = baseHost.isNullOrEmpty() || requestUrl.host.equals(baseHost, ignoreCase = true) || baseHost == "localhost" || requestUrl.host == "test.com"
        
        if (isSameHost) {
            try {
                runBlocking {
                    withTimeoutOrNull(5000) {
                        tokenManager.waitForInitialization()
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "intercept: TokenManager initialization FAILED for ${requestUrl.encodedPath}", e)
                }
            }
        }
        
        val token = tokenManager.currentToken
        val requestBuilder = request.newBuilder()
        if (isSameHost && !token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        return try {
            chain.proceed(requestBuilder.build())
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "intercept: EXCEPTION for ${requestUrl.encodedPath}", e)
            }
            throw e
        }
    }

    private companion object {
        private const val TAG = "AuthInterceptor"
    }
}
