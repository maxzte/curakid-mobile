package com.misterio.core.network.api

import com.misterio.core.network.dto.UpdateUserRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class UserApiTest : BaseApiTest<UserApi>() {

    override fun createApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Test
    fun `getCurrentUser sends correct request and parses response`() = runTest {
        enqueueResponse("""
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "email": "user@example.com",
                "name": "John Doe",
                "role": "USER",
                "onboardingCompleted": true,
                "active": true
            }
        """.trimIndent())

        val response = api.getCurrentUser()
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/users/me", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals("John Doe", response.body()?.name)
        assertEquals("user@example.com", response.body()?.email)
    }

    @Test
    fun `updateCurrentUser sends PUT with body and returns updated user`() = runTest {
        enqueueResponse("""
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "email": "user@example.com",
                "name": "John Smith",
                "role": "USER",
                "onboardingCompleted": true,
                "active": true
            }
        """.trimIndent())

        val response = api.updateCurrentUser(UpdateUserRequest("John Smith"))
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/users/me", request.path)
        assertEquals("PUT", request.method)
        assertTrue(response.isSuccessful)
        assertEquals("John Smith", response.body()?.name)
    }

    @Test
    fun `deleteCurrentUser sends DELETE request`() = runTest {
        enqueueResponse("", 204)

        val response = api.deleteCurrentUser()
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/users/me", request.path)
        assertEquals("DELETE", request.method)
        assertTrue(response.isSuccessful)
    }
}
