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

class UpdateCurrentUserUseCaseTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val useCase = UpdateCurrentUserUseCase(userRepository)

    @Test
    fun invoke_blankName_returnsFailure() = runTest {
        val result = useCase("   ")
        assertTrue(result.isFailure)
    }

    @Test
    fun invoke_validName_returnsUpdatedUser() = runTest {
        val user = User(
            id = UUID.randomUUID(),
            email = "test@example.com",
            name = "Valid Name",
            role = "USER",
            onboardingCompleted = true,
            active = true
        )
        coEvery { userRepository.updateCurrentUser("Valid Name") } returns Result.success(user)

        val result = useCase("Valid Name")

        assertTrue(result.isSuccess)
        assertEquals("Valid Name", result.getOrNull()?.name)
    }
}
