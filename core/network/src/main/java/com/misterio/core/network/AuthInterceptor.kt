package com.misterio.core.network

import com.misterio.core.common.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.accessToken.firstOrNull()
        }
        val rtoken = runBlocking {
            tokenManager.refreshToken.firstOrNull()
        }
        val request = chain.request().newBuilder()
        if (token != null) {
            println("TOKEN: $token")
            println("rTOKEN: $rtoken")
            request.header("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}
