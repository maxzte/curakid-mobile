package com.misterio.curakid.feature.signup

import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.VerifyEmailUseCase
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
class VerifyEmailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val verifyEmailUseCase: VerifyEmailUseCase = mockk()

    private fun createViewModel() = VerifyEmailViewModel(
        verifyEmailUseCase = verifyEmailUseCase
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
    fun verifyEmail_success_emitsSuccessState() = runTest(testDispatcher) {
        val email = "user@example.com"
        val code = "123456"
        coEvery { verifyEmailUseCase(email, code) } returns Result.success(Unit)

        val viewModel = createViewModel()

        viewModel.verifyEmail(email, code)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        coVerify { verifyEmailUseCase(email, code) }
    }

    @Test
    fun verifyEmail_failure_emitsErrorState() = runTest(testDispatcher) {
        val email = "user@example.com"
        val code = "000000"
        val error = DomainError.BadRequestError("Invalid code")
        coEvery { verifyEmailUseCase(email, code) } returns Result.failure(error)

        val viewModel = createViewModel()

        viewModel.verifyEmail(email, code)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Error)
        assertEquals(error, (viewModel.uiState.value as UiState.Error).error)
    }

    @Test
    fun resetState_resetsUiStateToInitial() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.resetState()

        assertEquals(UiState.Initial, viewModel.uiState.value)
    }
}
