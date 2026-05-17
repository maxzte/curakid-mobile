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
import com.misterio.core.domain.model.VaccineEvent
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.GetVaccineEventsUseCase
import com.misterio.core.domain.usecase.SyncEventsUseCase
import com.misterio.core.domain.usecase.SyncVaccineEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val selectedChild: Child? = null,
    val vaccineEvent: VaccineEvent? = null,
    val doctorEvent: Event? = null,
    val upcomingItems: List<Event> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getChildUseCase: GetChildUseCase,
    getVaccineEventsUseCase: GetVaccineEventsUseCase,
    private val getEventsUseCase: GetEventsUseCase,
    private val syncVaccineEventsUseCase: SyncVaccineEventsUseCase,
    private val syncEventsUseCase: SyncEventsUseCase,
) : ViewModel() {

    private val selectedChild = getChildUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val vaccineEvents = selectedChild.flatMapLatest { child ->
        if (child != null) {
            viewModelScope.launch {
                syncVaccineEventsUseCase(child.id)
            }
            getVaccineEventsUseCase(child.id)
        } else {
            flowOf(emptyList())
        }
    }

    private val events = selectedChild.flatMapLatest { child ->
        if (child != null) {
            viewModelScope.launch {
                syncEventsUseCase(child.id)
            }
            getEventsUseCase(child.id)
        } else {
            flowOf(emptyList())
        }
    }

    val uiState: StateFlow<UiState<HomeUiState>> = combine(
        selectedChild,
        vaccineEvents,
        events
    ) { child, vList, eList ->
        val vaccineEvents = vList
            .filter { it.status != EventStatus.DONE }
            .map { vaccine ->
                Event(
                    id = vaccine.id,
                    title = vaccine.name,
                    date = vaccine.date,
                    type = EventType.VACCINE,
                    description = "Доза ${vaccine.doseNumber}/${vaccine.totalDoses}",
                    status = vaccine.status,
                )
            }

        Log.d(TAG, "childId: ${child?.id}, vaccineEvents: ${vList.size}, userEvents: ${eList.size}")

        // Select items for Hero Cards
        val v = vList.firstOrNull { it.status != EventStatus.DONE }
        val e = eList.firstOrNull { it.type == EventType.DOCTOR } ?: eList.firstOrNull()

        val allUpcoming = (vaccineEvents + eList)
            .filter { it.date.toLocalDate() >= LocalDate.now() }
            .sortedBy { it.date }
        Log.d(TAG, "allUpcoming $$allUpcoming")
        Log.d(TAG, "docEvent $$e")

        // Skip items shown in Hero Cards
        val upcomingItems = allUpcoming.filter { it.id != v?.id && it.id != e?.id }

        HomeUiState(
            selectedChild = child,
            vaccineEvent = v,
            doctorEvent = e,
            upcomingItems = upcomingItems
        )
    }.asUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Initial
        )

    init {
        Log.i(TAG, "INIT")
    }

    private companion object {
        private const val TAG = "HomeVM"
    }
}
