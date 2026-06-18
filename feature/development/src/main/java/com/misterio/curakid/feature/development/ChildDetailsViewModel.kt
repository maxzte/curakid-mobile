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
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.usecase.AddMeasurementUseCase
import com.misterio.core.domain.usecase.GetAiReportByIdUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetMeasurementNormsUseCase
import com.misterio.core.domain.usecase.GetMeasurementsUseCase
import com.misterio.core.domain.usecase.PollReportStatusUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val measurementRepository: MeasurementRepository,
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

    private val childFlow = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .onEach { child ->
            child?.let {
                polledReportId = null // Reset polling state for new child
                runCatching {
                    measurementRepository.syncMeasurementsForChild(it.id)
                }
                prefetchNorms(it)
            }
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    private fun prefetchNorms(child: Child) {
        viewModelScope.launch {
            val ageInMonths = ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble() / 30.44
            
            val typesToFetch = mutableListOf(
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
                        _normsCache.value += (type to norms)
                    }
                }
            }
        }
    }

    val n: StateFlow<List<ChartNormPoint>> = combine(
        _selectedChartType,
        childFlow.filterNotNull(),
        _normsCache
    ) { type, child, cache ->
        if (cache.containsKey(type)) {
            return@combine cache[type]!!
        }

        val ageInMonths = ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble() / 30.44

        val res = getMeasurementNormsUseCase(
            chartType = type.toDomain(ageInMonths),
            gender = child.gender
        )
        val norms = if (res.isSuccess) res.getOrThrow() else emptyList()
        
        if (norms.isNotEmpty()) {
            _normsCache.value += (type to norms)
        }
        norms
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
            if (measurement.aiReportId != null) {
                triggerReportPolling(measurement.aiReportId!!)
                getAiReportByIdUseCase(measurement.aiReportId!!)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun triggerReportPolling(reportId: UUID) {
        if (polledReportId == reportId) return
        polledReportId = reportId

        viewModelScope.launch {
            _reportLoading.value = true
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

    fun onChartTypeChange(type: GrowthChartType) {
        _selectedChartType.value = type
    }

    private fun GrowthChartType.toDomain(ageInMonths: Double = 0.0) = when (this) {
        GrowthChartType.WeightForAge -> ChartType.WFA
        GrowthChartType.HeightForAge -> ChartType.HFA
        GrowthChartType.WeightForHeight -> if (ageInMonths < 24.0) ChartType.WFL else ChartType.WFH
        GrowthChartType.BMI -> ChartType.BMI
        GrowthChartType.HeadCircumference -> ChartType.HCFA
    }
}
