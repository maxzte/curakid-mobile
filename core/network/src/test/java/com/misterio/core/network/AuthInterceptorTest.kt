package com.misterio.core.network

import com.misterio.curakid.core.common.TokenManager
import android.util.Log
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val tokenManager: TokenManager = mockk(relaxed = true)
    private lateinit var authInterceptor: AuthInterceptor
    private val chain: Interceptor.Chain = mockk()
    private val request: Request = Request.Builder().url("http://test.com").build()
    private val response: Response = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0

        authInterceptor = AuthInterceptor(tokenManager)
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns response
        every { response.code } returns 200
    }

    @Test
    fun `intercept should wait for initialization and add token when available`() {
        // Given
        val token = "test-token"
        coEvery { tokenManager.waitForInitialization() } returns Unit
        every { tokenManager.currentToken } returns token

        // When
        val result = authInterceptor.intercept(chain)

        // Then
        verify { chain.proceed(match { it.header("Authorization") == "Bearer $token" }) }
        assertEquals(response, result)
    }

    @Test
    fun `intercept should NOT add token when it is null`() {
        // Given
        coEvery { tokenManager.waitForInitialization() } returns Unit
        every { tokenManager.currentToken } returns null

        // When
        authInterceptor.intercept(chain)

        // Then
        verify { chain.proceed(match { it.header("Authorization") == null }) }
    }

    @Test
    fun `intercept should NOT add token when it is empty`() {
        // Given
        coEvery { tokenManager.waitForInitialization() } returns Unit
        every { tokenManager.currentToken } returns ""

        // When
        authInterceptor.intercept(chain)

        // Then
        verify { chain.proceed(match { it.header("Authorization") == null }) }
    }
    
    @Test(expected = Exception::class)
    fun `intercept should throw exception when chain proceed fails`() {
        // Given
        coEvery { tokenManager.waitForInitialization() } returns Unit
        every { tokenManager.currentToken } returns "token"
        every { chain.proceed(any()) } throws Exception("Network error")

        // When
        authInterceptor.intercept(chain)
    }
}
