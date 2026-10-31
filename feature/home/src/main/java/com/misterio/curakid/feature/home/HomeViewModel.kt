@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.home

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.GetHomeEventsOverviewUseCase
import com.misterio.core.domain.usecase.GetNotificationPermissionRequestedUseCase
import com.misterio.core.domain.usecase.SetNotificationPermissionRequestedUseCase
import com.misterio.core.domain.usecase.SyncHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HomeUiState(
    val selectedChild: Child? = null,
    val vaccineEvent: Event.Vaccine? = null,
    val doctorEvent: Event? = null,
    val upcomingItems: List<Event> = emptyList(),
    val aiReports: List<LifePeriodAiReport> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getChildUseCase: GetChildUseCase,
    private val getEventsUseCase: GetEventsUseCase,
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase,
    private val getHomeEventsOverviewUseCase: GetHomeEventsOverviewUseCase,
    private val syncHomeDataUseCase: SyncHomeDataUseCase,
    getNotificationPermissionRequestedUseCase: GetNotificationPermissionRequestedUseCase,
    private val setNotificationPermissionRequestedUseCase: SetNotificationPermissionRequestedUseCase,
) : ViewModel() {

    val notificationPermissionRequested: StateFlow<Boolean> = getNotificationPermissionRequestedUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    fun onNotificationPermissionHandled() {
        viewModelScope.launch {
            setNotificationPermissionRequestedUseCase(true)
        }
    }

    private val selectedChild = getChildUseCase()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val events = selectedChild
        .flatMapLatest { child ->
            if (child != null) {
                getEventsUseCase(child.id)
            } else {
                flowOf(emptyList())
            }
        }

    private val aiReports = selectedChild
        .flatMapLatest { child ->
            if (child != null) {
                getChildAiReportsUseCase(child.id)
            } else {
                flowOf(emptyList())
            }
        }

    private val _isInitialSyncing = MutableStateFlow(true)
    val isInitialSyncing: StateFlow<Boolean> = _isInitialSyncing.asStateFlow()

    val uiState: StateFlow<UiState<HomeUiState>> = combine(
        selectedChild,
        events,
        aiReports,
        _isInitialSyncing
    ) { child, eList, reports, syncing ->
        Log.d(TAG, "childId: ${child?.id}, totalEvents: ${eList.size}, reports: ${reports.size}, syncing: $syncing")

        if (child == null && syncing) {
            return@combine null
        }

        val overview = getHomeEventsOverviewUseCase(eList)

        HomeUiState(
            selectedChild = child,
            vaccineEvent = overview.vaccineEvent,
            doctorEvent = overview.doctorEvent,
            upcomingItems = overview.upcomingItems,
            aiReports = reports
        )
    }.flatMapLatest { state ->
        if (state == null) {
            flowOf(UiState.Loading)
        } else {
            flowOf(UiState.Success(state))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UiState.Loading
    )

    init {
        Log.d(TAG, "INIT")
        viewModelScope.launch {
            try {
                syncHomeDataUseCase(childId = null, force = false)
            } finally {
                _isInitialSyncing.value = false
            }
        }
        viewModelScope.launch {
            selectedChild.collectLatest { child ->
                if (child != null) {
                    syncHomeDataUseCase(childId = child.id, force = false)
                }
            }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                syncHomeDataUseCase(childId = selectedChild.value?.id, force = true)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private companion object {
        private const val TAG = "HomeVM"
    }
}
