package com.misterio.core.network.api

import com.misterio.core.network.dto.LoginRequest
import com.misterio.core.network.dto.RefreshTokenRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class AuthApiTest : BaseApiTest<AuthApi>() {

    override fun createApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Test
    fun `login sends correct request and parses response`() = runTest {
        enqueueResponse("""
            {
                "accessToken": "access-token",
                "refreshToken": "refresh-token",
                "onboardingCompleted": true
            }
        """.trimIndent())

        val response = api.login(LoginRequest("test@test.com", "password"))
        val request = mockWebServer.takeRequest()

        assertEquals("/api/auth/login", request.path)
        assertEquals("POST", request.method)
        assertTrue(response.isSuccessful)
        assertEquals("access-token", response.body()?.accessToken)
        assertEquals(true, response.body()?.onboardingCompleted)
    }

    @Test
    fun `refresh sends correct request`() = runTest {
        enqueueResponse("""
            {
                "accessToken": "new-access-token",
                "refreshToken": "new-refresh-token",
                "onboardingCompleted": true
            }
        """.trimIndent())

        val response = api.refresh(RefreshTokenRequest("old-refresh-token"))
        val request = mockWebServer.takeRequest()

        assertEquals("/api/auth/refresh", request.path)
        assertEquals("POST", request.method)
        assertTrue(response.isSuccessful)
        assertEquals("new-access-token", response.body()?.accessToken)
    }
}
