package com.misterio.core.data.repository

import com.misterio.core.domain.repository.NotificationRepository
import com.misterio.core.network.api.NotificationApi
import com.misterio.core.network.dto.RegisterDeviceTokenRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NotificationRepositoryImplTest {

    private val notificationApi: NotificationApi = mockk(relaxed = true)
    private lateinit var repository: NotificationRepository

    @Before
    fun setUp() {
        repository = NotificationRepositoryImpl(notificationApi)
    }

    @Test
    fun `registerDeviceToken returns Success when api returns successful response`() = runTest {
        val token = "sample_fcm_token"
        coEvery {
            notificationApi.registerDeviceToken(RegisterDeviceTokenRequest(token = token, deviceType = "ANDROID"))
        } returns Response.success(mockk(relaxed = true))

        val result = repository.registerDeviceToken(token)

        assertTrue(result.isSuccess)
        coVerify {
            notificationApi.registerDeviceToken(RegisterDeviceTokenRequest(token = token, deviceType = "ANDROID"))
        }
    }

    @Test
    fun `registerDeviceToken returns Failure when api returns error response`() = runTest {
        val token = "sample_fcm_token"
        coEvery {
            notificationApi.registerDeviceToken(any())
        } returns Response.error(500, "Server Error".toResponseBody())

        val result = repository.registerDeviceToken(token)

        assertTrue(result.isFailure)
    }

    @Test
    fun `removeDeviceToken returns Success when api returns 204`() = runTest {
        val token = "token_to_remove"
        coEvery {
            notificationApi.removeDeviceToken(token)
        } returns Response.success(Unit)

        val result = repository.removeDeviceToken(token)

        assertTrue(result.isSuccess)
        coVerify { notificationApi.removeDeviceToken(token) }
    }
}
