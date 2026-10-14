package com.misterio.curakid.feature.signup

import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.LoginUseCase
import com.misterio.core.domain.usecase.OAuthLoginUseCase
import io.mockk.coEvery
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
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val loginUseCase: LoginUseCase = mockk()
    private val oAuthLoginUseCase: OAuthLoginUseCase = mockk()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginUseCase, oAuthLoginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is UiState Initial`() {
        assertEquals(UiState.Initial, viewModel.uiState.value)
    }

    @Test
    fun `login success updates state to Success`() = runTest(testDispatcher) {
        coEvery { loginUseCase("test@example.com", "password123") } returns Result.success(Unit)

        viewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        assertEquals("Login successful", (viewModel.uiState.value as UiState.Success).data)
    }

    @Test
    fun `login failure updates state to Error`() = runTest(testDispatcher) {
        val error = DomainError.AuthenticationError("Invalid credentials")
        coEvery { loginUseCase("test@example.com", "wrong") } returns Result.failure(error)

        viewModel.login("test@example.com", "wrong")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Error)
        assertEquals(error, (viewModel.uiState.value as UiState.Error).error)
    }

    @Test
    fun `oAuthLogin success updates state to Success`() = runTest(testDispatcher) {
        coEvery { oAuthLoginUseCase("token_123") } returns Result.success(Unit)

        viewModel.oAuthLogin("token_123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
    }

    @Test
    fun `resetState resets uiState to Initial`() = runTest(testDispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.success(Unit)

        viewModel.login("a@b.com", "pass")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UiState.Success)

        viewModel.resetState()
        assertEquals(UiState.Initial, viewModel.uiState.value)
    }
}
