@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.feature.development

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.usecase.AddMeasurementUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetMeasurementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildDetailsViewModel @Inject constructor(
    private val getMeasurementsUseCase: GetMeasurementsUseCase,
    private val addMeasurementUseCase: AddMeasurementUseCase,
    private val getChildUseCase: GetChildUseCase,
    private val measurementRepository: MeasurementRepository,
    private val assessmentRepository: AssessmentRepository,
) : ViewModel() {

    init {
        Log.i("ChildDetailsVM", "INIT")
    }

    init {
        viewModelScope.launch {
            getChildUseCase().collectLatest { child ->
                child?.let {
                    measurementRepository.syncMeasurementsForChild(child.id)
                    assessmentRepository.syncAssessments(child.id)
                }
            }
        }
    }

    val selectedChild: StateFlow<Child?> = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val measurements: StateFlow<List<Measurement>> = selectedChild
        .filterNotNull()
        .flatMapLatest { child ->
            measurementRepository.getMeasurementsForChild(child.id)
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun measurements(childId: String): StateFlow<List<Measurement>> =
        getMeasurementsUseCase(childId)
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    fun syncMeasurements(childId: String) {
        viewModelScope.launch {
            measurementRepository.syncMeasurementsForChild(childId)
        }
    }

    fun addMeasurement(weight: Double?, height: Double?, headCirc: Double?) {
        viewModelScope.launch {
            addMeasurementUseCase(selectedChild.value?.id!!, weight, height, headCirc)
        }
    }
}