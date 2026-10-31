package com.misterio.curakid.feature.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.ui.components.CuraKidPullToRefreshBox
import com.misterio.core.ui.components.ErrorView
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun HomeScreen(
    onEventClick: (eventId: UUID) -> Unit,
    onVaccinationClick: (vaccinationId: UUID) -> Unit,
    onShowAllUpcoming: () -> Unit,
    onChatClick: () -> Unit,
    onAiReportClick: (reportId: UUID) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val notificationPermissionRequested by viewModel.notificationPermissionRequested.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showPermissionDialog by remember(notificationPermissionRequested) {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        mutableStateOf(!notificationPermissionRequested && !hasPermission)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.onNotificationPermissionHandled()
    }

    if (showPermissionDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NotificationPermissionDialog(
            onEnableClick = {
                showPermissionDialog = false
                viewModel.onNotificationPermissionHandled()
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onLaterClick = {
                showPermissionDialog = false
                viewModel.onNotificationPermissionHandled()
            }
        )
    }

    when (val state = uiState) {
        is UiState.Loading, UiState.Initial -> LoadingScreen()
        is UiState.Success -> {
            val data = state.data
            CuraKidPullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() }
            ) {
                HomeContent(
                    childName = data.selectedChild?.name.orEmpty(),
                    nextVaccination = data.vaccineEvent,
                    nextDoctorVisit = data.doctorEvent,
                    upcomingList = data.upcomingItems,
                    aiReports = data.aiReports,
                    onEventClick = onEventClick,
                    onVaccinationClick = onVaccinationClick,
                    onShowAllUpcoming = onShowAllUpcoming,
                    onChatClick = onChatClick,
                    onAiReportClick = onAiReportClick
                )
            }
        }
        is UiState.Error -> {
            ErrorView(
                error = state.error,
                onRetry = { viewModel.refresh() }
            )
        }
    }
}

@Composable
fun HomeContent(
    childName: String,
    nextVaccination: Event.Vaccine?,
    nextDoctorVisit: Event?,
    upcomingList: List<Event>,
    aiReports: List<LifePeriodAiReport>,
    onEventClick: (eventId: UUID) -> Unit,
    onVaccinationClick: (UUID) -> Unit,
    onShowAllUpcoming: () -> Unit,
    onChatClick: () -> Unit,
    onAiReportClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
        contentPadding = rememberListContentPadding()
    ) {
        itemWrapper {
            ScreenHeader(
                title = stringResource(R.string.home_app_name),
                childName = childName,
                onChatClick = onChatClick
            )
        }

        if (nextVaccination != null || nextDoctorVisit != null) {
            item {
                HeroCardsPager(
                    vaccination = nextVaccination,
                    doctorVisit = nextDoctorVisit,
                    onVaccinationClick = onVaccinationClick,
                    onDoctorVisitClick = onEventClick
                )
            }
        }

        if (upcomingList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.home_upcoming),
                    actionLabel = stringResource(R.string.home_all),
                    onAction = onShowAllUpcoming
                )
            }

            itemWrapper {
                UpcomingList(
                    items = upcomingList,
                    onItemClick = { onEventClick(it.id) }
                )
            }
        }

        if (aiReports.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.home_reports_and_recommendations),
                    actionLabel = "",
                    onAction = {}
                )
            }

            item {
                AiReportsRow(
                    reports = aiReports,
                    onItemClick = onAiReportClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    CurakidTheme {
        HomeContent(
            childName = "Марія",
            nextVaccination = null,
            nextDoctorVisit = null,
            upcomingList = emptyList(),
            aiReports = emptyList(),
            onEventClick = {},
            onVaccinationClick = {},
            onShowAllUpcoming = {},
            onChatClick = {},
            onAiReportClick = {}
        )
    }
}
