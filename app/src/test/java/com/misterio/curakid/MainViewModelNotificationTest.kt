package com.misterio.curakid

import com.misterio.core.domain.model.AuthState
import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.sync.SyncController
import com.misterio.core.domain.usecase.GetAuthStateUseCase
import com.misterio.core.domain.usecase.GetIsPasscodeEnabledUseCase
import com.misterio.core.domain.usecase.LogoutUseCase
import com.misterio.core.domain.usecase.RegisterFcmTokenUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MainViewModelNotificationTest {

    private val getAuthStateUseCase: GetAuthStateUseCase = mockk()
    private val getIsPasscodeEnabledUseCase: GetIsPasscodeEnabledUseCase = mockk()
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase = mockk(relaxed = true)
    private val connectivityRepository: ConnectivityRepository = mockk(relaxed = true)
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)
    private val syncController: SyncController = mockk(relaxed = true)

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        every { getAuthStateUseCase() } returns flowOf(AuthState.AUTHENTICATED)
        every { getIsPasscodeEnabledUseCase() } returns flowOf(false)
        every { connectivityRepository.isConnected } returns MutableStateFlow(true)
        every { syncController.isSyncing } returns MutableStateFlow(false)

        viewModel = MainViewModel(
            getAuthStateUseCase = getAuthStateUseCase,
            getIsPasscodeEnabledUseCase = getIsPasscodeEnabledUseCase,
            registerFcmTokenUseCase = registerFcmTokenUseCase,
            connectivityRepository = connectivityRepository,
            logoutUseCase = logoutUseCase,
            syncController = syncController,
        )
    }

    @Test
    fun setPendingEventId_validUuidString_updatesPendingEventId() {
        val eventUuid = UUID.randomUUID()
        viewModel.setPendingEventId(eventUuid.toString())

        assertEquals(eventUuid, viewModel.pendingEventId.value)

        viewModel.clearPendingEventId()
        assertNull(viewModel.pendingEventId.value)
    }

    @Test
    fun setPendingEventId_nullOrInvalidString_leavesPendingEventIdUnchanged() {
        assertNull(viewModel.pendingEventId.value)

        viewModel.setPendingEventId(null)
        assertNull(viewModel.pendingEventId.value)

        viewModel.setPendingEventId("not-a-valid-uuid")
        assertNull(viewModel.pendingEventId.value)
    }

    @Test
    fun setPendingReportId_validUuidString_updatesPendingReportId() {
        val reportUuid = UUID.randomUUID()
        viewModel.setPendingReportId(reportUuid.toString())

        assertEquals(reportUuid, viewModel.pendingReportId.value)

        viewModel.clearPendingReportId()
        assertNull(viewModel.pendingReportId.value)
    }

    @Test
    fun setPendingReportId_nullOrInvalidString_leavesPendingReportIdUnchanged() {
        assertNull(viewModel.pendingReportId.value)

        viewModel.setPendingReportId(null)
        assertNull(viewModel.pendingReportId.value)

        viewModel.setPendingReportId("invalid-report-uuid")
        assertNull(viewModel.pendingReportId.value)
    }
}
