@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.development

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.asUiState
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.domain.usecase.AddMeasurementUseCase
import com.misterio.core.domain.usecase.GetAiReportByIdUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetMeasurementNormsUseCase
import com.misterio.core.domain.usecase.GetMeasurementsUseCase
import com.misterio.core.domain.usecase.PollReportStatusUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import com.misterio.core.domain.usecase.SyncMeasurementsUseCase
import com.misterio.core.domain.util.suspendRunCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChildDetailsViewModel @Inject constructor(
    private val getMeasurementsUseCase: GetMeasurementsUseCase,
    private val addMeasurementUseCase: AddMeasurementUseCase,
    getChildUseCase: GetChildUseCase,
    private val syncMeasurementsUseCase: SyncMeasurementsUseCase,
    private val pollReportStatusUseCase: PollReportStatusUseCase,
    private val getAiReportByIdUseCase: GetAiReportByIdUseCase,
    private val getMeasurementNormsUseCase: GetMeasurementNormsUseCase,
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase,
) : ViewModel() {

    private val _reportLoading = MutableStateFlow(false)
    val reportLoading: StateFlow<Boolean> = _reportLoading.asStateFlow()

    private val _selectedChartType = MutableStateFlow(GrowthChartType.WeightForAge)
    val selectedChartType: StateFlow<GrowthChartType> = _selectedChartType.asStateFlow()

    private val _normsCache = MutableStateFlow<Map<GrowthChartType, List<ChartNormPoint>>>(emptyMap())
    val normsCache = _normsCache.asStateFlow()

    private var polledReportId: UUID? = null
    private var pollingJob: Job? = null

    private val childFlow = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .onEach { child ->
            child?.let {
                polledReportId = null // Reset polling state for new child
                pollingJob?.cancel()
                syncMeasurementsUseCase(it.id)
                prefetchNorms(it)
            }
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    private fun prefetchNorms(child: Child) {
        viewModelScope.launch {
            val ageInMonths = ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble() / 30.44

            val typesToFetch = listOf(
                GrowthChartType.WeightForAge,
                GrowthChartType.HeightForAge,
                GrowthChartType.HeadCircumference,
                GrowthChartType.WeightForHeight
            )

            typesToFetch.forEach { type ->
                if (!_normsCache.value.containsKey(type)) {
                    val res = getMeasurementNormsUseCase(
                        chartType = type.toDomain(ageInMonths),
                        gender = child.gender
                    )
                    res.getOrNull()?.let { norms ->
                        _normsCache.update { it + (type to norms) }
                    }
                }
            }
        }
    }

    val norms: StateFlow<List<ChartNormPoint>> = combine(
        _selectedChartType,
        childFlow.filterNotNull(),
        _normsCache
    ) { type, _, cache ->
        cache[type] ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            combine(_selectedChartType, childFlow.filterNotNull()) { type, child ->
                type to child
            }.collect { (type, child) ->
                if (!_normsCache.value.containsKey(type)) {
                    val ageInMonths = ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble() / 30.44
                    val res = getMeasurementNormsUseCase(
                        chartType = type.toDomain(ageInMonths),
                        gender = child.gender
                    )
                    res.getOrNull()?.let { normsList ->
                        _normsCache.update { it + (type to normsList) }
                    }
                }
            }
        }
    }

    // Keep backwards compatibility for any external callers during migration
    val n: StateFlow<List<ChartNormPoint>> get() = norms

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
            (state as? UiState.Success)?.data?.firstOrNull { it.aiReportId != null }
        }
        .distinctUntilChanged()
        .flatMapLatest { measurement ->
            val reportId = measurement.aiReportId
            if (reportId != null) {
                triggerReportPolling(reportId)
                getAiReportByIdUseCase(reportId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun triggerReportPolling(reportId: UUID) {
        if (polledReportId == reportId) return
        polledReportId = reportId
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            _reportLoading.value = true
            suspendRunCatching {
                pollReportStatusUseCase(reportId).collect { result ->
                    result.onSuccess { isReady ->
                        if (isReady) {
                            val currentChild = (selectedChild.value as? UiState.Success<Child?>)?.data
                            currentChild?.let { syncChildAiReportsUseCase(it.id) }
                            _reportLoading.value = false
                        }
                    }.onFailure {
                        _reportLoading.value = false
                    }
                }
            }.onFailure {
                _reportLoading.value = false
            }
        }
    }

    fun addMeasurement(weight: Double?, height: Double?, headCirc: Double?) {
        viewModelScope.launch {
            val currentChildState = selectedChild.value
            val child = (currentChildState as? UiState.Success)?.data
            if (child != null) {
                addMeasurementUseCase(child.id, weight, height, headCirc)
                suspendRunCatching {
                    syncMeasurementsUseCase(child.id)
                }
            }
        }
    }

    fun onChartTypeChange(type: GrowthChartType) {
        _selectedChartType.value = type
    }

    fun getNormRange(type: GrowthChartType): ClosedFloatingPointRange<Float>? {
        val child = (selectedChild.value as? UiState.Success)?.data ?: return null
        val typeNorms = _normsCache.value[type] ?: return null

        val targetX = if (type == GrowthChartType.WeightForHeight) {
            val measurementsList = (measurements.value as? UiState.Success<List<Measurement>>)?.data ?: emptyList()
            measurementsList.lastOrNull()?.height ?: 75.0
        } else {
            ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble()
        }

        val nearest = typeNorms.minByOrNull { kotlin.math.abs(it.x - targetX) } ?: return null
        return nearest.p15.toFloat()..nearest.p85.toFloat()
    }

    private fun GrowthChartType.toDomain(ageInMonths: Double = 0.0) = when (this) {
        GrowthChartType.WeightForAge -> ChartType.WFA
        GrowthChartType.HeightForAge -> ChartType.HFA
        GrowthChartType.WeightForHeight -> if (ageInMonths < 24.0) ChartType.WFL else ChartType.WFH
        GrowthChartType.BMI -> ChartType.BMI
        GrowthChartType.HeadCircumference -> ChartType.HCFA
    }
}
