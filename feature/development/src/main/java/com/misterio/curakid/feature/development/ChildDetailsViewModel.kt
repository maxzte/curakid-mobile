@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.development

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.asUiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.usecase.AddMeasurementUseCase
import com.misterio.core.domain.usecase.GetAiReportByIdUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetMeasurementsUseCase
import com.misterio.core.domain.usecase.PollReportStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildDetailsViewModel @Inject constructor(
    private val getMeasurementsUseCase: GetMeasurementsUseCase,
    private val addMeasurementUseCase: AddMeasurementUseCase,
    getChildUseCase: GetChildUseCase,
    private val measurementRepository: MeasurementRepository,
    private val pollReportStatusUseCase: PollReportStatusUseCase,
    private val getAiReportByIdUseCase: GetAiReportByIdUseCase,
) : ViewModel() {

    private val _reportLoading = MutableStateFlow(false)
    val reportLoading: StateFlow<Boolean> = _reportLoading.asStateFlow()

    private var polledReportId: String? = null

    private val childFlow = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .onEach { child ->
            child?.let {
                measurementRepository.syncMeasurementsForChild(it.id)
            }
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    val selectedChild: StateFlow<UiState<Child?>> = childFlow
        .asUiState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val measurements: StateFlow<UiState<List<Measurement>>> = childFlow
        .flatMapLatest { child ->
            if (child != null) getMeasurementsUseCase(child.id) else flowOf(emptyList())
        }
        .asUiState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val lastAiReport: StateFlow<MeasurementAiReport?> = measurements
        .mapNotNull { state ->
            (state as? UiState.Success)?.data?.lastOrNull { it.aiReportId != null }
        }
        .distinctUntilChanged()
        .flatMapLatest { measurement ->
            if (measurement.aiReportId != null) {
                triggerReportPolling(measurement.aiReportId!!)
                getAiReportByIdUseCase(measurement.aiReportId!!)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun triggerReportPolling(reportId: String) {
        if (polledReportId == reportId) return
        polledReportId = reportId

        viewModelScope.launch {
            _reportLoading.value = true
            pollReportStatusUseCase(reportId).collect { result ->
                result.onSuccess { isReady ->
                    if (isReady) {
                        _reportLoading.value = false
                    }
                }.onFailure {
                    _reportLoading.value = false
                }
            }
        }
    }

    fun syncMeasurements(childId: String) {
        viewModelScope.launch {
            measurementRepository.syncMeasurementsForChild(childId)
        }
    }

    fun addMeasurement(weight: Double?, height: Double?, headCirc: Double?) {
        viewModelScope.launch {
            val currentChildState = selectedChild.value
            if (currentChildState is UiState.Success && currentChildState.data != null) {
                val child = currentChildState.data
                addMeasurementUseCase(child?.id!!, weight, height, headCirc)
                measurementRepository.syncMeasurementsForChild(child.id)
            }
        }
    }
}
