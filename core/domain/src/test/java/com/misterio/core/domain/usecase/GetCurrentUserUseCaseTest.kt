package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.User
import com.misterio.core.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class GetCurrentUserUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val useCase = GetCurrentUserUseCase(userRepository)

    @Test
    fun invoke_success_returnsUser() = runTest {
        val user = User(
            id = UUID.randomUUID(),
            email = "test@example.com",
            name = "Test",
            role = "USER",
            onboardingCompleted = true,
            active = true
        )
        coEvery { userRepository.getCurrentUser() } returns Result.success(user)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }
}
