package com.misterio.core.network.api

import com.misterio.core.network.dto.RegisterDeviceTokenRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class NotificationApiTest : BaseApiTest<NotificationApi>() {

    override fun createApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Test
    fun `registerDeviceToken sends correct request and parses response`() = runTest {
        enqueueResponse("""
            {
                "id": "e4c5b96a-04b7-4c8d-8a58-38b4ef21b369",
                "token": "fcm_test_token_sample",
                "deviceType": "ANDROID",
                "lastActiveAt": "2026-10-20T10:00:00Z"
            }
        """.trimIndent())

        val requestDto = RegisterDeviceTokenRequest(
            token = "fcm_test_token_sample",
            deviceType = "ANDROID"
        )
        val response = api.registerDeviceToken(requestDto)
        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("/api/v1/notifications/device-token", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertTrue(response.isSuccessful)
        assertEquals("fcm_test_token_sample", response.body()?.token)
        assertEquals("ANDROID", response.body()?.deviceType)
    }

    @Test
    fun `removeDeviceToken sends DELETE request with token query param`() = runTest {
        enqueueResponse("", code = 204)

        val tokenToRemove = "token_to_delete_123"
        val response = api.removeDeviceToken(tokenToRemove)
        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("/api/v1/notifications/device-token?token=$tokenToRemove", recordedRequest.path)
        assertEquals("DELETE", recordedRequest.method)
        assertTrue(response.isSuccessful)
        assertEquals(204, response.code())
    }
}
