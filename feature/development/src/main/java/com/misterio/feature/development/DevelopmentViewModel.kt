@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.misterio.feature.development

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.usecase.AddMeasurementUseCase
import com.misterio.core.domain.usecase.CompleteLifePeriodUseCase
import com.misterio.core.domain.usecase.GetChildAssessmentsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetLifePeriodUseCase
import com.misterio.core.domain.usecase.SaveAssessmentDraftUseCase
import com.misterio.core.domain.usecase.SyncChildrenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.Period
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevelopmentUiState(
    val currentPeriod: LifePeriod? = null,
    val answers: Map<String, String> = emptyMap(), // questionId -> selectedValue
    val domainToAssessmentId: Map<String, String> = emptyMap(), // domain -> assessmentId
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val completionResult: CompletePeriodResult? = null,
    val showEarlyTransitionDialog: Boolean = false,
    val lifePeriods: List<LifePeriod> = emptyList()
)

@HiltViewModel
class DevelopmentViewModel @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val assessmentRepository: AssessmentRepository,
    private val getChildUseCase: GetChildUseCase,
    private val syncChildrenUseCase: SyncChildrenUseCase,
    private val addMeasurementUseCase: AddMeasurementUseCase,
    private val getLifePeriodUseCase: GetLifePeriodUseCase,
    private val getChildAssessmentsUseCase: GetChildAssessmentsUseCase,
    private val saveAssessmentDraftUseCase: SaveAssessmentDraftUseCase,
    private val completeLifePeriodUseCase: CompleteLifePeriodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevelopmentUiState())
    val uiState: StateFlow<DevelopmentUiState> = _uiState.asStateFlow()

    private val _answersFlow = MutableStateFlow<Map<String, String>>(emptyMap())

    val selectedChild: StateFlow<Child?> = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        Log.i("DevelopmentVM", "INIT")
        loadInitialData()
        setupAutosave()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("DevelopmentVM", "DESTROY")
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            selectedChild.collectLatest { child ->
                // Clear state for new child or if child becomes null
                _uiState.update { 
                    it.copy(
                        currentPeriod = null,
                        answers = emptyMap(),
                        domainToAssessmentId = emptyMap(),
                        completionResult = null,
                        error = null,
                        lifePeriods = emptyList()
                    )
                }
                _answersFlow.value = emptyMap()
                
                if (child != null) {
                    assessmentRepository.syncAssessments(child.id)
                    loadLifePeriods(child)
                }
            }
        }
    }

    private suspend fun loadLifePeriods(child: Child) {
        val result = assessmentRepository.getLifePeriods()
        Log.i("DEVELvm", "$result")
        if (result.isSuccess) {
            val periods = result.getOrThrow()
            _uiState.update { it.copy(lifePeriods = periods) }
            
            val ageInMonths = Period.between(child.birthDate, LocalDate.now()).toTotalMonths().toInt()
            val matchingPeriod = periods.find { (ageInMonths >= it.minMonths) && (ageInMonths <= it.maxMonths) }
            
            if (_uiState.value.currentPeriod == null && periods.isNotEmpty()) {
                selectPeriod((matchingPeriod ?: periods.first()).code)
            }
        }
    }

    fun selectPeriod(periodCode: String) {
        val child = selectedChild.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val periodResult = getLifePeriodUseCase(periodCode)
            val assessmentsResult = getChildAssessmentsUseCase(child.id)
            
            if (periodResult.isSuccess && assessmentsResult.isSuccess) {
                val period = periodResult.getOrThrow()
                val assessments = assessmentsResult.getOrThrow()
                
                val domainMap = assessments.associate { it.domain to it.id }
                val initialAnswers = mutableMapOf<String, String>()
                
                assessments.forEach { assessment ->
                    assessment.rawData.forEach { (qId, value) ->
                        if (value is String) initialAnswers[qId] = value
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        currentPeriod = period,
                        answers = initialAnswers,
                        domainToAssessmentId = domainMap,
                        isLoading = false
                    )
                }
                _answersFlow.value = initialAnswers
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = periodResult.exceptionOrNull()?.message ?: assessmentsResult.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun onAnswerSelected(questionId: String, value: String) {
        _uiState.update { state ->
            val newAnswers = state.answers + (questionId to value)
            _answersFlow.value = newAnswers
            state.copy(answers = newAnswers)
        }
    }

    private fun setupAutosave() {
        _answersFlow
            .debounce(1000)
            .distinctUntilChanged()
            .onEach { answers ->
                val child = selectedChild.value ?: return@onEach
                if (answers.isEmpty()) return@onEach
                saveDraft(child.id, answers)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun saveDraft(childId: String, answers: Map<String, String>) {
        val period = _uiState.value.currentPeriod ?: return
        
        val domainToQuestions = period.questions.groupBy { it.domain }
        
        domainToQuestions.forEach { (domain, questions) ->
            val domainAnswers = questions
                .filter { answers.containsKey(it.id) }
                .associate { it.id to answers[it.id]!! }
            
            if (domainAnswers.isNotEmpty()) {
                val existingId = _uiState.value.domainToAssessmentId[domain]
                performSaveWithRetry(childId, domain, domainAnswers, existingId)
            }
        }
    }

    private suspend fun performSaveWithRetry(
        childId: String,
        domain: String,
        rawData: Map<String, String>,
        existingId: String?,
        attempt: Int = 1
    ) {
        _uiState.update { it.copy(isSaving = true) }
        val result = saveAssessmentDraftUseCase(childId, domain, rawData, existingId)
        
        if (result.isSuccess) {
            val savedAssessment = result.getOrThrow()
            _uiState.update { state ->
                state.copy(
                    domainToAssessmentId = state.domainToAssessmentId + (domain to savedAssessment.id),
                    isSaving = false
                )
            }
        } else {
            if (attempt < 3) {
                delay(1000L * attempt)
                performSaveWithRetry(childId, domain, rawData, existingId, attempt + 1)
            } else {
                _uiState.update { it.copy(isSaving = false, error = "Failed to autosave $domain") }
            }
        }
    }

    fun completePeriod(allowEarlyTransition: Boolean = false) {
        val child = selectedChild.value ?: return
        val period = _uiState.value.currentPeriod ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, showEarlyTransitionDialog = false) }
            val result = completeLifePeriodUseCase(child.id, period.code, allowEarlyTransition)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, completionResult = result.getOrThrow()) }
            } else {
                val error = result.exceptionOrNull()
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = error?.message,
                        showEarlyTransitionDialog = error?.message?.contains("early", ignoreCase = true) == true
                    )
                }
            }
        }
    }

    fun dismissEarlyTransitionDialog() {
        _uiState.update { it.copy(showEarlyTransitionDialog = false) }
    }

    fun addMeasurement(weight: Double?, height: Double?, headCirc: Double?) {
        val childId = selectedChild.value?.id ?: return
        viewModelScope.launch {
            addMeasurementUseCase(childId, weight, height, headCirc)
        }
    }
}
