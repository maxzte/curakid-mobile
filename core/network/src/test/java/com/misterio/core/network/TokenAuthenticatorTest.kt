package com.misterio.core.network

import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.dto.AuthResponse
import com.misterio.core.network.dto.RefreshTokenRequest
import com.misterio.curakid.core.common.TokenManager
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response as RetrofitResponse

@OptIn(ExperimentalCoroutinesApi::class)
class TokenAuthenticatorTest {

    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val authApi: AuthApi = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        authenticator = TokenAuthenticator(tokenManager, authApi, testScope)
    }

    @Test
    fun `authenticate returns null when too many retries`() {
        val request = Request.Builder().url("http://test.com").build()
        val priorResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(priorResponse)
            .build()

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }

    @Test
    fun `authenticate returns null when no refresh token`() {
        val request = Request.Builder().url("http://test.com").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
        every { tokenManager.currentRefreshToken } returns null

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }

    @Test
    fun `authenticate refreshes token and returns new request`() = runTest(testDispatcher) {
        val request = Request.Builder()
            .url("http://test.com")
            .header("Authorization", "Bearer old-token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        every { tokenManager.currentToken } returns "old-token"
        every { tokenManager.currentRefreshToken } returns "refresh-token"
        
        val newTokenResponse = AuthResponse(accessToken = "new-token", refreshToken = "new-refresh-token", onboardingCompleted = true)
        coEvery { authApi.refresh(RefreshTokenRequest("refresh-token")) } returns RetrofitResponse.success(newTokenResponse)

        val result = authenticator.authenticate(null, response)

        coVerify { tokenManager.save("new-token", "new-refresh-token") }
        assertEquals("Bearer new-token", result?.header("Authorization"))
    }

    @Test
    fun `authenticate uses existing token if already refreshed by another thread`() {
        val request = Request.Builder()
            .url("http://test.com")
            .header("Authorization", "Bearer old-token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        every { tokenManager.currentToken } returns "new-token-from-elsewhere"
        every { tokenManager.currentRefreshToken } returns "refresh-token"

        val result = authenticator.authenticate(null, response)

        assertEquals("Bearer new-token-from-elsewhere", result?.header("Authorization"))
        coVerify(exactly = 0) { authApi.refresh(any()) }
    }

    @Test
    fun `authenticate clears tokens on critical refresh failure`() = runTest(testDispatcher) {
        val request = Request.Builder()
            .url("http://test.com")
            .header("Authorization", "Bearer old-token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        every { tokenManager.currentToken } returns "old-token"
        every { tokenManager.currentRefreshToken } returns "refresh-token"
        
        coEvery { authApi.refresh(any()) } returns RetrofitResponse.error(401, mockk(relaxed = true))

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        coVerify { tokenManager.clearTokens() }
    }
    
    @Test
    fun `authenticate returns null on refresh exception`() = runTest(testDispatcher) {
        val request = Request.Builder()
            .url("http://test.com")
            .header("Authorization", "Bearer old-token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        every { tokenManager.currentToken } returns "old-token"
        every { tokenManager.currentRefreshToken } returns "refresh-token"
        
        coEvery { authApi.refresh(any()) } throws Exception("Network error")

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }
}
