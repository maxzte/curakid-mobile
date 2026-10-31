package com.misterio.curakid.feature.profile

import com.misterio.core.domain.usecase.DeleteProfileUseCase
import com.misterio.core.domain.usecase.GetIsNotificationsEnabledUseCase
import com.misterio.core.domain.usecase.GetIsPasscodeEnabledUseCase
import com.misterio.core.domain.usecase.ToggleNotificationsUseCase
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
    private val getIsNotificationsEnabledUseCase: GetIsNotificationsEnabledUseCase = mockk()
    private val toggleNotificationsUseCase: ToggleNotificationsUseCase = mockk(relaxed = true)
    private val getIsPasscodeEnabledUseCase: GetIsPasscodeEnabledUseCase = mockk()
    private val deleteProfileUseCase: DeleteProfileUseCase = mockk(relaxed = true)

    private fun createViewModel() = SettingsViewModel(
        getIsNotificationsEnabledUseCase = getIsNotificationsEnabledUseCase,
        toggleNotificationsUseCase = toggleNotificationsUseCase,
        getIsPasscodeEnabledUseCase = getIsPasscodeEnabledUseCase,
        deleteProfileUseCase = deleteProfileUseCase,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getIsNotificationsEnabledUseCase() } returns flowOf(true)
        every { getIsPasscodeEnabledUseCase() } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun observeSettings_toggleNotifications_updatesStateAndCallsUseCase() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val job1 = launch { viewModel.isNotificationsEnabled.collect {} }
        val job2 = launch { viewModel.isPasscodeEnabled.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.isNotificationsEnabled.value)
        assertFalse(viewModel.isPasscodeEnabled.value)

        viewModel.setNotificationsEnabled(false)
        advanceUntilIdle()

        coVerify { toggleNotificationsUseCase(false) }

        job1.cancel()
        job2.cancel()
    }

    @Test
    fun setShowDeleteDialog_flagChanged_updatesState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setShowDeleteDialog(true)
        assertTrue(viewModel.showDeleteDialog.value)

        viewModel.setShowDeleteDialog(false)
        assertFalse(viewModel.showDeleteDialog.value)
    }

    @Test
    fun deleteAccount_invoked_callsDeleteProfileUseCase() = runTest(testDispatcher) {
        coEvery { deleteProfileUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteAccount()
        advanceUntilIdle()

        coVerify { deleteProfileUseCase() }
    }
}
