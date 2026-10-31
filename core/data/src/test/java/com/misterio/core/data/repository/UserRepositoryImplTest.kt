package com.misterio.core.data.repository

import com.misterio.core.domain.repository.UserRepository
import com.misterio.core.network.api.UserApi
import com.misterio.core.network.dto.UpdateUserRequest
import com.misterio.core.network.dto.UserResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.util.UUID

class UserRepositoryImplTest {

    private val userApi: UserApi = mockk(relaxed = true)
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        repository = UserRepositoryImpl(userApi)
    }

    @Test
    fun getCurrentUser_success_returnsUser() = runTest {
        val userId = UUID.randomUUID()
        val userResponse = UserResponse(
            id = userId,
            email = "test@example.com",
            name = "Test User",
            role = "USER",
            onboardingCompleted = true,
            active = true
        )
        coEvery { userApi.getCurrentUser() } returns Response.success(userResponse)

        val result = repository.getCurrentUser()

        assertTrue(result.isSuccess)
        assertEquals("Test User", result.getOrNull()?.name)
        assertEquals(userId, result.getOrNull()?.id)
        coVerify { userApi.getCurrentUser() }
    }

    @Test
    fun updateCurrentUser_success_returnsUpdatedUser() = runTest {
        val userId = UUID.randomUUID()
        val updatedResponse = UserResponse(
            id = userId,
            email = "test@example.com",
            name = "New Name",
            role = "USER",
            onboardingCompleted = true,
            active = true
        )
        coEvery { userApi.updateCurrentUser(any()) } returns Response.success(updatedResponse)

        val result = repository.updateCurrentUser("New Name")

        assertTrue(result.isSuccess)
        assertEquals("New Name", result.getOrNull()?.name)
        coVerify { userApi.updateCurrentUser(UpdateUserRequest("New Name")) }
    }

    @Test
    fun deleteCurrentUser_success_returnsSuccess() = runTest {
        coEvery { userApi.deleteCurrentUser() } returns Response.success(Unit)

        val result = repository.deleteCurrentUser()

        assertTrue(result.isSuccess)
        coVerify { userApi.deleteCurrentUser() }
    }
}
