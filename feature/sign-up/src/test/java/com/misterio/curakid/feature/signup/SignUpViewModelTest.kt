package com.misterio.curakid.feature.signup

import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.DownloadLegalDocumentUseCase
import com.misterio.core.domain.usecase.GetLegalDocumentsUseCase
import com.misterio.core.domain.usecase.OAuthLoginUseCase
import com.misterio.core.domain.usecase.RegisterUseCase
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
class SignUpViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val registerUseCase: RegisterUseCase = mockk()
    private val oAuthLoginUseCase: OAuthLoginUseCase = mockk()
    private val getLegalDocumentsUseCase: GetLegalDocumentsUseCase = mockk()
    private val downloadLegalDocumentUseCase: DownloadLegalDocumentUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getLegalDocumentsUseCase() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signUp success updates uiState to Success`() = runTest(testDispatcher) {
        coEvery { registerUseCase("test@example.com", "pass123", "User", true) } returns Result.success("Registered")

        val viewModel = SignUpViewModel(
            registerUseCase,
            oAuthLoginUseCase,
            getLegalDocumentsUseCase,
            downloadLegalDocumentUseCase
        )

        viewModel.signUp("test@example.com", "pass123", "User", true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        assertEquals("Registered", (viewModel.uiState.value as UiState.Success).data)
    }

    @Test
    fun `signUp failure updates uiState to Error`() = runTest(testDispatcher) {
        val error = DomainError.BadRequestError("Email already in use")
        coEvery { registerUseCase(any(), any(), any(), any()) } returns Result.failure(error)

        val viewModel = SignUpViewModel(
            registerUseCase,
            oAuthLoginUseCase,
            getLegalDocumentsUseCase,
            downloadLegalDocumentUseCase
        )

        viewModel.signUp("test@example.com", "pass123", "User", true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Error)
        assertEquals(error, (viewModel.uiState.value as UiState.Error).error)
    }
}
