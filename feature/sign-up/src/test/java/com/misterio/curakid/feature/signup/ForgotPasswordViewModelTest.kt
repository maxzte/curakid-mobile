package com.misterio.curakid.feature.signup

import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.ForgotPasswordUseCase
import com.misterio.core.domain.usecase.ResetPasswordUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val forgotPasswordUseCase: ForgotPasswordUseCase = mockk()
    private val resetPasswordUseCase: ResetPasswordUseCase = mockk()

    private fun createViewModel() = ForgotPasswordViewModel(
        forgotPasswordUseCase = forgotPasswordUseCase,
        resetPasswordUseCase = resetPasswordUseCase,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun requestReset_success_transitionsToResetPasswordStep() = runTest(testDispatcher) {
        val email = "user@example.com"
        coEvery { forgotPasswordUseCase(email) } returns Result.success("Code sent")

        val viewModel = createViewModel()
        assertEquals(ForgotPasswordStep.RequestEmail, viewModel.step.value)

        viewModel.requestReset(email)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.ResetPassword, viewModel.step.value)
        assertEquals(UiState.Initial, viewModel.uiState.value)
        coVerify { forgotPasswordUseCase(email) }
    }

    @Test
    fun requestReset_failure_emitsErrorState() = runTest(testDispatcher) {
        val email = "user@example.com"
        val error = DomainError.NotFoundError("Email not found")
        coEvery { forgotPasswordUseCase(email) } returns Result.failure(error)

        val viewModel = createViewModel()

        viewModel.requestReset(email)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.RequestEmail, viewModel.step.value)
        assertTrue(viewModel.uiState.value is UiState.Error)
        assertEquals(error, (viewModel.uiState.value as UiState.Error).error)
    }

    @Test
    fun resetPassword_success_transitionsToSuccessStep() = runTest(testDispatcher) {
        val email = "user@example.com"
        val code = "123456"
        val newPassword = "newPassword123"
        coEvery { resetPasswordUseCase(email, code, newPassword) } returns Result.success("Password reset")

        val viewModel = createViewModel()

        viewModel.resetPassword(email, code, newPassword)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.Success, viewModel.step.value)
        assertTrue(viewModel.uiState.value is UiState.Success)
        coVerify { resetPasswordUseCase(email, code, newPassword) }
    }
}
