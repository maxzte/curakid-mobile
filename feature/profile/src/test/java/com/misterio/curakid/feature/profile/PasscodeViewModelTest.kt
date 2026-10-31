package com.misterio.curakid.feature.profile

import com.misterio.core.domain.usecase.ClearPasscodeUseCase
import com.misterio.core.domain.usecase.SavePasscodeUseCase
import com.misterio.core.domain.usecase.VerifyPasscodeUseCase
import com.misterio.curakid.core.common.PasscodeMode
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PasscodeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val savePasscodeUseCase: SavePasscodeUseCase = mockk(relaxed = true)
    private val verifyPasscodeUseCase: VerifyPasscodeUseCase = mockk(relaxed = true)
    private val clearPasscodeUseCase: ClearPasscodeUseCase = mockk(relaxed = true)

    private fun createViewModel(mode: PasscodeMode) = PasscodeViewModel(
        mode = mode,
        savePasscodeUseCase = savePasscodeUseCase,
        verifyPasscodeUseCase = verifyPasscodeUseCase,
        clearPasscodeUseCase = clearPasscodeUseCase,
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
    fun onDigitClick_digitsEntered_updatesPasscodeState() = runTest(testDispatcher) {
        val viewModel = createViewModel(PasscodeMode.SETUP)

        viewModel.onDigitClick("1")
        viewModel.onDigitClick("2")

        assertEquals("12", viewModel.uiState.value.passcode)
    }

    @Test
    fun onDeleteClick_digitsPresent_removesLastDigit() = runTest(testDispatcher) {
        val viewModel = createViewModel(PasscodeMode.SETUP)

        viewModel.onDigitClick("1")
        viewModel.onDigitClick("2")
        viewModel.onDeleteClick()

        assertEquals("1", viewModel.uiState.value.passcode)
    }

    @Test
    fun setupMode_fourDigitsEntered_transitionsToConfirmingState() = runTest(testDispatcher) {
        val viewModel = createViewModel(PasscodeMode.SETUP)

        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isConfirming)
        assertEquals("1234", viewModel.uiState.value.confirmPasscode)
        assertEquals("", viewModel.uiState.value.passcode)
    }

    @Test
    fun setupMode_matchingConfirmation_savesPasscodeAndEmitsSuccess() = runTest(testDispatcher) {
        val viewModel = createViewModel(PasscodeMode.SETUP)

        // First pass
        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        // Confirmation pass
        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        coVerify { savePasscodeUseCase("1234") }
    }

    @Test
    fun unlockMode_correctPasscode_verifiesSuccessfully() = runTest(testDispatcher) {
        coEvery { verifyPasscodeUseCase("1234") } returns true
        val viewModel = createViewModel(PasscodeMode.UNLOCK)

        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.failedAttempts)
        coVerify { verifyPasscodeUseCase("1234") }
    }

    @Test
    fun unlockMode_fiveFailedAttempts_triggersLockout() = runTest(testDispatcher) {
        coEvery { verifyPasscodeUseCase(any()) } returns false
        val viewModel = createViewModel(PasscodeMode.UNLOCK)

        // Attempts 1 to 4: each has 1000ms delay to clear error
        repeat(4) {
            listOf("9", "9", "9", "9").forEach { viewModel.onDigitClick(it) }
            testScheduler.advanceTimeBy(1050)
            testScheduler.runCurrent()
        }

        // Attempt 5: triggers lockout immediately
        listOf("9", "9", "9", "9").forEach { viewModel.onDigitClick(it) }
        testScheduler.advanceTimeBy(10)
        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isLockedOut)
        assertEquals(30, viewModel.uiState.value.lockoutSecondsRemaining)
    }

    @Test
    fun unlockMode_biometricSuccess_resetsFailedAttempts() = runTest(testDispatcher) {
        val viewModel = createViewModel(PasscodeMode.UNLOCK)

        viewModel.onBiometricSuccess()
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.failedAttempts)
        assertFalse(viewModel.uiState.value.isLockedOut)
    }
}
