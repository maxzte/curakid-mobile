package com.misterio.curakid.feature.profile

import com.misterio.curakid.core.common.PasscodeMode
import com.misterio.curakid.core.common.SecurityManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
    private val securityManager: SecurityManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `digit clicks update passcode in state`() = runTest(testDispatcher) {
        val viewModel = PasscodeViewModel(PasscodeMode.SETUP, securityManager)

        viewModel.onDigitClick("1")
        viewModel.onDigitClick("2")

        assertEquals("12", viewModel.uiState.value.passcode)
    }

    @Test
    fun `delete click removes last digit`() = runTest(testDispatcher) {
        val viewModel = PasscodeViewModel(PasscodeMode.SETUP, securityManager)

        viewModel.onDigitClick("1")
        viewModel.onDigitClick("2")
        viewModel.onDeleteClick()

        assertEquals("1", viewModel.uiState.value.passcode)
    }

    @Test
    fun `SETUP mode - first 4 digits transition to confirming state`() = runTest(testDispatcher) {
        val viewModel = PasscodeViewModel(PasscodeMode.SETUP, securityManager)

        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isConfirming)
        assertEquals("1234", viewModel.uiState.value.confirmPasscode)
        assertEquals("", viewModel.uiState.value.passcode)
    }

    @Test
    fun `SETUP mode - matching confirmation saves passcode and sends Success`() = runTest(testDispatcher) {
        val viewModel = PasscodeViewModel(PasscodeMode.SETUP, securityManager)

        // First pass
        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        // Confirmation pass
        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        coVerify { securityManager.savePasscode("1234") }
    }

    @Test
    fun `UNLOCK mode - correct passcode sends Success event`() = runTest(testDispatcher) {
        coEvery { securityManager.verifyPasscode("1234") } returns true
        val viewModel = PasscodeViewModel(PasscodeMode.UNLOCK, securityManager)

        listOf("1", "2", "3", "4").forEach { viewModel.onDigitClick(it) }
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.failedAttempts)
        coVerify { securityManager.verifyPasscode("1234") }
    }

    @Test
    fun `UNLOCK mode - 5 failed attempts trigger lockout`() = runTest(testDispatcher) {
        coEvery { securityManager.verifyPasscode(any()) } returns false
        val viewModel = PasscodeViewModel(PasscodeMode.UNLOCK, securityManager, testDispatcher)

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
    fun `UNLOCK mode - biometric success sends Success event and resets failed attempts`() = runTest(testDispatcher) {
        val viewModel = PasscodeViewModel(PasscodeMode.UNLOCK, securityManager, testDispatcher)

        viewModel.onBiometricSuccess()
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.failedAttempts)
        assertFalse(viewModel.uiState.value.isLockedOut)
    }
}
