package com.misterio.curakid

import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.repository.NotificationRepository
import com.misterio.core.domain.usecase.LogoutUseCase
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.curakid.core.common.SecurityManager
import com.misterio.curakid.core.common.TokenManager
import com.misterio.curakid.core.common.TokenState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MainViewModelNotificationTest {

    private val appDataStore: AppDataStore = mockk(relaxed = true)
    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val connectivityRepository: ConnectivityRepository = mockk(relaxed = true)
    private val securityManager: SecurityManager = mockk(relaxed = true)
    private val notificationRepository: NotificationRepository = mockk(relaxed = true)
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)

    private val tokenStateFlow = MutableStateFlow<TokenState>(TokenState.Empty)

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        every { tokenManager.tokenState } returns tokenStateFlow
        every { connectivityRepository.isConnected } returns MutableStateFlow(true)
        every { appDataStore.onboardingCompleted } returns MutableStateFlow(true)
        every { securityManager.isPasscodeEnabled } returns MutableStateFlow(false)

        viewModel = MainViewModel(
            appDataStore = appDataStore,
            tokenManager = tokenManager,
            connectivityRepository = connectivityRepository,
            securityManager = securityManager,
            notificationRepository = notificationRepository,
            logoutUseCase = logoutUseCase
        )
    }

    @Test
    fun `setPendingEventId parses valid UUID string into pendingEventId StateFlow`() {
        val eventUuid = UUID.randomUUID()
        viewModel.setPendingEventId(eventUuid.toString())

        assertEquals(eventUuid, viewModel.pendingEventId.value)

        viewModel.clearPendingEventId()
        assertNull(viewModel.pendingEventId.value)
    }

    @Test
    fun `setPendingEventId with null or invalid string does not crash and leaves pendingEventId unchanged`() {
        assertNull(viewModel.pendingEventId.value)

        viewModel.setPendingEventId(null)
        assertNull(viewModel.pendingEventId.value)

        viewModel.setPendingEventId("not-a-valid-uuid")
        assertNull(viewModel.pendingEventId.value)
    }

    @Test
    fun `setPendingReportId parses valid UUID string into pendingReportId StateFlow`() {
        val reportUuid = UUID.randomUUID()
        viewModel.setPendingReportId(reportUuid.toString())

        assertEquals(reportUuid, viewModel.pendingReportId.value)

        viewModel.clearPendingReportId()
        assertNull(viewModel.pendingReportId.value)
    }

    @Test
    fun `setPendingReportId with null or invalid string does not crash and leaves pendingReportId unchanged`() {
        assertNull(viewModel.pendingReportId.value)

        viewModel.setPendingReportId(null)
        assertNull(viewModel.pendingReportId.value)

        viewModel.setPendingReportId("invalid-report-uuid")
        assertNull(viewModel.pendingReportId.value)
    }
}
