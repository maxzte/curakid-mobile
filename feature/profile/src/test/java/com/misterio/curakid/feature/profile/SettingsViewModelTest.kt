package com.misterio.curakid.feature.profile

import com.misterio.core.domain.usecase.DeleteProfileUseCase
import com.misterio.curakid.core.common.SecurityManager
import com.misterio.curakid.core.common.SettingsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsManager: SettingsManager = mockk(relaxed = true)
    private val securityManager: SecurityManager = mockk(relaxed = true)
    private val deleteProfileUseCase: DeleteProfileUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observes settings and toggle notifications`() = runTest(testDispatcher) {
        every { settingsManager.isNotificationsEnabled } returns flowOf(true)
        every { securityManager.isPasscodeEnabled } returns flowOf(false)

        val viewModel = SettingsViewModel(settingsManager, securityManager, deleteProfileUseCase)
        val job1 = launch { viewModel.isNotificationsEnabled.collect {} }
        val job2 = launch { viewModel.isPasscodeEnabled.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.isNotificationsEnabled.value)
        assertFalse(viewModel.isPasscodeEnabled.value)

        viewModel.setNotificationsEnabled(false)
        advanceUntilIdle()

        coVerify { settingsManager.saveNotificationsEnabled(false) }

        job1.cancel()
        job2.cancel()
    }

    @Test
    fun `setShowDeleteDialog updates state`() = runTest(testDispatcher) {
        every { settingsManager.isNotificationsEnabled } returns flowOf(true)
        every { securityManager.isPasscodeEnabled } returns flowOf(false)

        val viewModel = SettingsViewModel(settingsManager, securityManager, deleteProfileUseCase)
        advanceUntilIdle()

        viewModel.setShowDeleteDialog(true)
        assertTrue(viewModel.showDeleteDialog.value)

        viewModel.setShowDeleteDialog(false)
        assertFalse(viewModel.showDeleteDialog.value)
    }

    @Test
    fun `deleteAccount calls deleteProfileUseCase`() = runTest(testDispatcher) {
        every { settingsManager.isNotificationsEnabled } returns flowOf(true)
        every { securityManager.isPasscodeEnabled } returns flowOf(false)
        coEvery { deleteProfileUseCase() } returns Result.success(Unit)

        val viewModel = SettingsViewModel(settingsManager, securityManager, deleteProfileUseCase)
        advanceUntilIdle()

        viewModel.deleteAccount()
        advanceUntilIdle()

        coVerify { deleteProfileUseCase() }
    }
}
