@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.misterio.curakid.feature.development

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.model.Option
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.usecase.CompleteLifePeriodUseCase
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildAssessmentsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetLifePeriodsUseCase
import com.misterio.core.domain.usecase.GetMeasurementsUseCase
import com.misterio.core.domain.usecase.PollReportStatusUseCase
import com.misterio.core.domain.usecase.SaveAssessmentDraftUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import com.misterio.core.domain.usecase.SyncChildAssessmentsUseCase
import com.misterio.core.domain.usecase.SyncLifePeriodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class AnswerUI(
    val assessmentId: String?,
    val questionId: String,
    val option: Option?,
    val originalOptionId: String? = null
)

data class DevelopmentUiState(
    val currentPeriod: LifePeriod? = null,
    val currentAiReport: LifePeriodAiReport? = null,
    val answers: List<AnswerUI> = emptyList(),
    val isLoading: Boolean = false,
    val isPollingReport: Boolean = false,
    val isCompleted: Boolean = false,
    val error: DomainError? = null,
    val isSaving: Boolean = false,
    val completionResult: CompletePeriodResult? = null,
    val showEarlyTransitionDialog: Boolean = false,
    val lifePeriods: List<LifePeriod> = emptyList(),
    val aiReports: List<LifePeriodAiReport> = emptyList(),
    val measurements: List<Measurement> = emptyList(),
    val pendingAnswers: Map<String, Answer> = emptyMap()
)

@HiltViewModel
class DevelopmentViewModel @Inject constructor(
    private val getLifePeriodsUseCase: GetLifePeriodsUseCase,
    private val getChildAssessmentsUseCase: GetChildAssessmentsUseCase,
    private val syncLifePeriodsUseCase: SyncLifePeriodsUseCase,
    private val syncChildAssessmentsUseCase: SyncChildAssessmentsUseCase,
    private val completeLifePeriodUseCase: CompleteLifePeriodUseCase,
    private val getMeasurementsUseCase: GetMeasurementsUseCase,
    getChildUseCase: GetChildUseCase,
    private val saveAssessmentDraftUseCase: SaveAssessmentDraftUseCase,
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase,
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase,
    private val pollReportStatusUseCase: PollReportStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevelopmentUiState())
    val uiState: StateFlow<DevelopmentUiState> = _uiState.asStateFlow()

    val selectedChild: StateFlow<Child?> = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _saveTrigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        Log.i(TAG, "INIT")
        observeData()
        observeAutoSave()
    }

    private fun observeData() {
        viewModelScope.launch {
            Log.d(TAG, "observeData: Starting flow")
            selectedChild
                .flatMapLatest { child ->
                    if (child == null) {
                        Log.d(TAG, "observeData: Child is null")
                        flowOf(null)
                    } else {
                        Log.d(TAG, "observeData: Child selected ${child.id}, triggering sync")
                        // Trigger sync in background
                        launch {
                            runCatching {
                                syncLifePeriodsUseCase()
                                syncChildAssessmentsUseCase(childId = child.id)
                                syncChildAiReportsUseCase(childId = child.id)
                            }.onFailure { e ->
                                Log.e(TAG, "observeData: Sync error", e)
                                _uiState.update { it.copy(error = DomainError.fromThrowable(e)) }
                            }
                        }

                        combine(
                            getLifePeriodsUseCase(),
                            getChildAssessmentsUseCase(childId = child.id),
                            getChildAiReportsUseCase(childId = child.id),
                            getMeasurementsUseCase(childId = child.id),
                        ) { periods, assessments, reports, measurements ->
                            DataSnapshot(periods, assessments, reports, measurements, child)
                        }
                    }
                }
                .collectLatest { snapshot ->
                    if (snapshot == null) return@collectLatest

                    Log.d(
                        TAG,
                        "observeData: New snapshot - Periods: ${snapshot.periods.size}, Assessments: ${snapshot.assessments.size}, Reports: ${snapshot.reports.size}, Measurements: ${snapshot.measurements.size}"
                    )

                    val dbAnswers = mutableListOf<AnswerUI>()
                    snapshot.assessments.forEach { assessment: ChildAssessment ->
                        assessment.answers.forEach { ans ->
                            val qId = ans.question?.id ?: return@forEach
                            val opt = ans.selectedOption ?: return@forEach
                            dbAnswers.add(
                                AnswerUI(
                                    assessmentId = assessment.id,
                                    questionId = qId,
                                    option = opt,
                                    originalOptionId = opt.id
                                )
                            )
                        }
                    }

                    _uiState.update { state ->
                        // Merge dbAnswers with local modifications that are still pending
                        val mergedAnswers = dbAnswers.toMutableList()
                        state.pendingAnswers.forEach { (qId, pending) ->
                            val existingIndex = mergedAnswers.indexOfFirst { it.questionId == qId }
                            val period = state.currentPeriod ?: snapshot.periods.firstOrNull()
                            val option = period?.questions?.find { it.id == qId }?.options?.find { it.id == pending.selectedOptionId }
                            
                            if (option != null) {
                                if (existingIndex != -1) {
                                    mergedAnswers[existingIndex] = mergedAnswers[existingIndex].copy(option = option)
                                } else {
                                    mergedAnswers.add(AnswerUI(assessmentId = null, questionId = qId, option = option))
                                }
                            }
                        }

                        val currentPeriod = state.currentPeriod ?: run {
                            val ageInMonths =
                                Period.between(snapshot.child.birthDate, LocalDate.now())
                                    .toTotalMonths()
                                    .toInt()

                            snapshot.periods.find { ageInMonths >= it.minMonths && ageInMonths <= it.maxMonths }
                                ?: snapshot.periods.firstOrNull()
                        }

                        val currentAiReport =
                            snapshot.reports.find { it.lifePeriodId == currentPeriod?.id }

                        Log.d(TAG, "observeData: Mapping for period ${currentPeriod?.id}")
                        Log.d(TAG, "observeData: found report=${currentAiReport?.id}")
                        if (currentAiReport == null && snapshot.reports.isNotEmpty()) {
                            Log.d(
                                TAG,
                                "observeData: Available reports: ${snapshot.reports.map { "id=${it.id}, periodId=${it.lifePeriodId}" }}"
                            )
                        }

                        state.copy(
                            lifePeriods = snapshot.periods,
                            aiReports = snapshot.reports,
                            measurements = snapshot.measurements,
                            currentPeriod = currentPeriod,
                            currentAiReport = currentAiReport,
                            answers = mergedAnswers,
                            isLoading = false,
                            isCompleted = currentAiReport != null
                        )
                    }
                }
        }
    }

    private data class DataSnapshot(
        val periods: List<LifePeriod>,
        val assessments: List<ChildAssessment>,
        val reports: List<LifePeriodAiReport>,
        val measurements: List<Measurement>,
        val child: Child
    )

    private fun observeAutoSave() {
        viewModelScope.launch {
            _saveTrigger
                .debounce(1000L)
                .collectLatest {
                    val childId = selectedChild.value?.id ?: return@collectLatest
                    val state = _uiState.value
                    val answersToSave = state.pendingAnswers.toMap()
                    if (answersToSave.isEmpty()) return@collectLatest

                    _uiState.update { it.copy(isSaving = true) }
                    
                    answersToSave.forEach { (qId, answer) ->
                        runCatching {
                            saveAssessmentDraftUseCase(
                                periodId = state.currentPeriod?.id!!,
                                childId = childId,
                                answer = answer,
                                skillAssessmentId = state.answers.find { it.questionId == qId }?.assessmentId
                            )
                        }.onSuccess { result ->
                            result.onSuccess { _ ->
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        pendingAnswers = currentState.pendingAnswers - qId
                                    )
                                }
                            }.onFailure { e ->
                                 _uiState.update { it.copy(error = DomainError.fromThrowable(e)) }
                            }
                        }.onFailure { e ->
                             _uiState.update { it.copy(error = DomainError.fromThrowable(e)) }
                        }
                    }
                    
                    _uiState.update { it.copy(isSaving = false) }
                }
        }
    }

    fun selectPeriod(period: LifePeriod) {
        _uiState.update { state ->
            state.copy(
                currentPeriod = period,
                currentAiReport = state.aiReports.find { it.lifePeriodId == period.id }
            )
        }
    }

    fun onAnswerSelected(questionId: String, optionId: String) {
        val currentState = _uiState.value
        val existingAnswer = currentState.answers.find { it.questionId == questionId }

        if (existingAnswer?.option?.id == optionId) {
            return
        }

        val period = currentState.currentPeriod ?: return
        val question = period.questions.find { it.id == questionId } ?: return
        val option = question.options.find { it.id == optionId } ?: return

        if (existingAnswer?.originalOptionId == optionId) {
            _uiState.update { state ->
                val newAnswerUI = existingAnswer.copy(option = option)
                val newAnswers =
                    state.answers.filterNot { it.questionId == questionId } + newAnswerUI
                state.copy(answers = newAnswers)
            }
            return
        }

        _uiState.update { state ->
            val newAnswerUI = AnswerUI(
                assessmentId = existingAnswer?.assessmentId,
                questionId = questionId,
                option = option,
                originalOptionId = existingAnswer?.originalOptionId
            )
            val newAnswers = state.answers.filterNot { it.questionId == questionId } + newAnswerUI
            state.copy(
                answers = newAnswers,
                pendingAnswers = state.pendingAnswers + (questionId to Answer(questionId, optionId))
            )
        }
        _saveTrigger.tryEmit(Unit)
    }

    fun onAnswerCleared(questionId: String) {
        _uiState.update { state ->
            val newAnswers = state.answers.map {
                if (it.questionId == questionId) it.copy(option = null) else it
            }
            state.copy(answers = newAnswers)
        }
    }

    private fun validateCompletion(state: DevelopmentUiState): DomainError? {
        val period = state.currentPeriod ?: return null
        val totalQuestions = period.questions.size
        if (totalQuestions == 0) return null

        val answeredCount = state.answers.count { it.option != null && (it.assessmentId != null || state.pendingAnswers.containsKey(it.questionId)) }
        val percent = (answeredCount.toFloat() / totalQuestions.toFloat()) * 100

        val missingItems = mutableListOf<String>()

        if (percent < 80) {
            val remaining = kotlin.math.max(1, (totalQuestions * 0.8).toInt() - answeredCount)
            missingItems.add("відповісти ще хоча б на $remaining питань")
        }

        val birthDate = selectedChild.value?.birthDate
        val periodMeasurements = if (birthDate != null) {
            state.measurements.filter { m ->
                val measurementMonths = Period.between(birthDate, m.date.toLocalDate()).toTotalMonths().toInt()
                measurementMonths >= period.minMonths && measurementMonths <= period.maxMonths
            }
        } else {
            state.measurements
        }

        val hasWeight = periodMeasurements.any { it.weight != null }
        val hasHeight = periodMeasurements.any { it.height != null }
        val hasHeadCirc = periodMeasurements.any { it.headCirc != null }

        if (!hasWeight) missingItems.add("додати замір ваги")
        if (!hasHeight) missingItems.add("додати замір зросту")
        if (!hasHeadCirc) missingItems.add("додати замір обхвату голови")

        return if (missingItems.isNotEmpty()) {
            DomainError.UnknownError("Для повного аналізу рекомендується: " + missingItems.joinToString(", ") + ".")
        } else {
            null
        }
    }

    fun completePeriod(allowEarlyTransition: Boolean = false) {
        val childId = selectedChild.value?.id ?: return
        val period = _uiState.value.currentPeriod ?: return

        if (!allowEarlyTransition) {
            val validationError = validateCompletion(_uiState.value)
            if (validationError != null) {
                _uiState.update {
                    it.copy(
                        error = validationError,
                        showEarlyTransitionDialog = true
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    showEarlyTransitionDialog = false
                )
            }
            // ... (rest of the code)

            // Final save if pending
            val state = _uiState.value
            state.pendingAnswers.forEach { (qId, pending) ->
                saveAssessmentDraftUseCase(
                    periodId = state.currentPeriod?.id!!,
                    childId = childId,
                    skillAssessmentId = state.answers.find { it.questionId == qId }?.assessmentId,
                    answer = pending
                )
            }

            val result = completeLifePeriodUseCase(childId, period.code, allowEarlyTransition)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCompleted = true,
                        completionResult = result.getOrThrow()
                    )
                }
                waitForReport(
                    reportId = result.getOrThrow().reportId!!,
                    childId = childId
                )
            } else {
                val error = result.exceptionOrNull()
                val domainError = (error as? DomainError) ?: DomainError.UnknownError(error?.message ?: "Unknown error", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = domainError,
//                        showEarlyTransitionDialog = error?.message?.contains(
//                            "early",
//                            ignoreCase = true
//                        ) == true
                    )
                }
            }
        }
    }

    private fun waitForReport(reportId: String, childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPollingReport = true) }
            pollReportStatusUseCase(reportId).collect { result ->
                result.onSuccess { isReady ->
                    if (isReady) {
                        syncChildAiReportsUseCase(childId = childId)
                        _uiState.update { it.copy(isPollingReport = false) }
                    }
                }.onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Unknown error", error)
                    _uiState.update { it.copy(isPollingReport = false, error = domainError) }
                }
            }
        }
    }

    fun dismissEarlyTransitionDialog() {
        _uiState.update { it.copy(showEarlyTransitionDialog = false, error = null) }
    }

    private companion object {
        private const val TAG = "DevelopmentVM"
    }
}
