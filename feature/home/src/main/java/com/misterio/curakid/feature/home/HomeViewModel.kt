@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.asUiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import com.misterio.core.domain.usecase.SyncChildrenUseCase
import com.misterio.core.domain.usecase.SyncEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val selectedChild: Child? = null,
    val vaccineEvent: Event.Vaccine? = null,
    val doctorEvent: Event? = null,
    val upcomingItems: List<Event> = emptyList(),
    val aiReports: List<LifePeriodAiReport> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncChildrenUseCase: SyncChildrenUseCase,
    getChildUseCase: GetChildUseCase,
    private val getEventsUseCase: GetEventsUseCase,
    private val syncEventsUseCase: SyncEventsUseCase,
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase,
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase,
) : ViewModel() {

    init {
        Log.d(TAG, "INIT")
    }

    private val selectedChild = getChildUseCase()
        .distinctUntilChanged()
        .onStart { syncChildrenUseCase(false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val events = selectedChild
        .flatMapLatest { child ->
            if (child != null) {
                viewModelScope.launch {
                    syncEventsUseCase(child.id)
                }
                getEventsUseCase(child.id)
            } else {
                flowOf(emptyList())
            }
        }

    private val aiReports = selectedChild
        .flatMapLatest { child ->
            if (child != null) {
                viewModelScope.launch {
                    syncChildAiReportsUseCase(child.id)
                }
                getChildAiReportsUseCase(child.id)
            } else {
                flowOf(emptyList())
            }
        }

    val uiState: StateFlow<UiState<HomeUiState>> = combine(
        selectedChild,
        events,
        aiReports
    ) { child, eList, reports ->
        Log.d(TAG, "childId: ${child?.id}, totalEvents: ${eList.size}, reports: ${reports.size}")

        // Select items for Hero Cards
        val v = eList.filterIsInstance<Event.Vaccine>().firstOrNull { it.status != EventStatus.DONE }
        val e = eList.firstOrNull { it.type == EventType.DOCTOR && it.status != EventStatus.DONE }
            ?: eList.firstOrNull { it !is Event.Vaccine }

        val allUpcoming = eList
            .filter { it.date.toLocalDate() >= LocalDate.now() }
            .sortedBy { it.date }

        // Skip items shown in Hero Cards
        val upcomingItems = allUpcoming.filter { it.id != v?.id && it.id != e?.id }

        HomeUiState(
            selectedChild = child,
            vaccineEvent = v,
            doctorEvent = e,
            upcomingItems = upcomingItems,
            aiReports = reports
        )
    }.asUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Initial
        )

    private companion object {
        private const val TAG = "HomeVM"
    }
}
