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
import com.misterio.core.domain.model.PeriodReadiness
import com.misterio.core.domain.usecase.CompleteLifePeriodUseCase
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildAssessmentsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetLifePeriodsUseCase
import com.misterio.core.domain.usecase.GetMeasurementsUseCase
import com.misterio.core.domain.usecase.GetPeriodReadinessUseCase
import com.misterio.core.domain.usecase.PollReportStatusUseCase
import com.misterio.core.domain.usecase.SaveAssessmentDraftUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import com.misterio.core.domain.usecase.SyncChildAssessmentsUseCase
import com.misterio.core.domain.usecase.SyncLifePeriodsUseCase
import com.misterio.core.domain.util.suspendRunCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import java.util.UUID
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

data class AnswerUI(
    val assessmentId: UUID?,
    val questionId: UUID,
    val option: Option?,
    val originalOptionId: UUID? = null
)

data class DevelopmentUiState(
    val currentPeriod: LifePeriod? = null,
    val currentAiReport: LifePeriodAiReport? = null,
    val answers: List<AnswerUI> = emptyList(),
    val summary: MilestoneSummary = MilestoneSummary(0, 0, 0, 0),
    val showSummary: Boolean = false,
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
    val pendingAnswers: Map<UUID, Answer> = emptyMap(),
    val remainingQuestions: Int = 0,
    val isMinAnswersRequirementMet: Boolean = false,
    val readiness: PeriodReadiness? = null,
    val showAgeTransitionPrompt: Boolean = false,
)

internal fun getMeasurementValue(m: Measurement, type: GrowthChartType): Double? = when (type) {
    GrowthChartType.WeightForAge -> m.weight
    GrowthChartType.HeightForAge -> m.height
    GrowthChartType.WeightForHeight -> m.weight
    GrowthChartType.BMI -> if (m.weight != null && m.height != null && m.height!! > 0) {
        val w = m.weight!!
        val h = m.height!! / 100.0 // convert cm to meters
        w / h.pow(2.0)
    } else null
    GrowthChartType.HeadCircumference -> m.headCirc
}

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
    private val getPeriodReadinessUseCase: GetPeriodReadinessUseCase,
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
        observeChildSync()
        observeData()
        observeAutoSave()
    }

    private fun observeChildSync() {
        viewModelScope.launch {
            selectedChild.collectLatest { child ->
                if (child == null) return@collectLatest
                launch {
                    suspendRunCatching { syncLifePeriodsUseCase() }
                        .onFailure { Log.e(TAG, "Sync periods error", it) }
                }
                launch {
                    suspendRunCatching { syncChildAssessmentsUseCase(childId = child.id) }
                        .onFailure { Log.e(TAG, "Sync assessments error", it) }
                }
                launch {
                    suspendRunCatching { syncChildAiReportsUseCase(childId = child.id) }
                        .onFailure { Log.e(TAG, "Sync reports error", it) }
                }
            }
        }
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
                        Log.d(TAG, "observeData: Child selected ${child.id}")
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

                        val (remaining, met) = calculateRequirementMet(mergedAnswers, state.pendingAnswers, currentPeriod)

                        // Check age-based transition prompt
                        val ageInMonths =
                            Period.between(snapshot.child.birthDate, LocalDate.now())
                                .toTotalMonths()
                                .toInt()
                        
                        val shouldShowAgePrompt = currentPeriod != null && 
                                ageInMonths > currentPeriod.maxMonths && 
                                currentAiReport == null &&
                                !state.isCompleted

                        val summary = currentPeriod?.questions?.toSummary(mergedAnswers) ?: MilestoneSummary(0, 0, 0, 0)
                        val isAllAnswered = summary.completed == summary.totalQuestions && summary.totalQuestions > 0

                        state.copy(
                            lifePeriods = snapshot.periods,
                            aiReports = snapshot.reports,
                            measurements = snapshot.measurements,
                            currentPeriod = currentPeriod,
                            currentAiReport = currentAiReport,
                            answers = mergedAnswers,
                            summary = summary,
                            showSummary = state.showSummary || isAllAnswered,
                            isLoading = false,
                            isCompleted = currentAiReport != null,
                            remainingQuestions = remaining,
                            isMinAnswersRequirementMet = met,
                            showAgeTransitionPrompt = shouldShowAgePrompt
                        )
                    }

                    val activePeriod = _uiState.value.currentPeriod
                    if (activePeriod != null) {
                        fetchReadiness(snapshot.child.id, activePeriod.code)
                    }
                }
        }
    }

    private fun fetchReadiness(childId: UUID, periodCode: String) {
        viewModelScope.launch {
            getPeriodReadinessUseCase(childId, periodCode).onSuccess { readiness ->
                _uiState.update { it.copy(readiness = readiness) }
            }.onFailure { e ->
                Log.e(TAG, "fetchReadiness: Error", e)
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
                .debounce(1000L.milliseconds)
                .collectLatest {
                    val childId = selectedChild.value?.id ?: return@collectLatest
                    val state = _uiState.value
                    val answersToSave = state.pendingAnswers.toMap()
                    if (answersToSave.isEmpty()) return@collectLatest

                    _uiState.update { it.copy(isSaving = true) }
                    
                    answersToSave.forEach { (qId, answer) ->
                        val periodId = state.currentPeriod?.id ?: return@forEach
                        val periodCode = state.currentPeriod.code
                        val skillAssessmentId = state.answers.find { it.questionId == qId }?.assessmentId
                        val result = suspendRunCatching {
                            saveAssessmentDraftUseCase(
                                periodId = periodId,
                                childId = childId,
                                answer = answer,
                                skillAssessmentId = skillAssessmentId
                            )
                        }.fold(
                            onSuccess = { it },
                            onFailure = { Result.failure(it) }
                        )

                        result.onSuccess {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    pendingAnswers = currentState.pendingAnswers - qId
                                )
                            }
                            fetchReadiness(childId, periodCode)
                        }.onFailure { e ->
                            _uiState.update { it.copy(error = DomainError.fromThrowable(e)) }
                        }
                    }
                    
                    _uiState.update { it.copy(isSaving = false) }
                }
        }
    }

    fun selectPeriod(period: LifePeriod) {
        val child = selectedChild.value
        val ageInMonths = child?.let {
            Period.between(it.birthDate, LocalDate.now())
                .toTotalMonths()
                .toInt()
        } ?: 0

        _uiState.update { state ->
            val (remaining, met) = calculateRequirementMet(state.answers, state.pendingAnswers, period)
            val summary = period.questions.toSummary(state.answers)
            val isAllAnswered = summary.completed == summary.totalQuestions && summary.totalQuestions > 0
            val currentAiReport = state.aiReports.find { it.lifePeriodId == period.id }
            val shouldShowAgePrompt = ageInMonths > period.maxMonths && currentAiReport == null

            state.copy(
                currentPeriod = period,
                currentAiReport = currentAiReport,
                isCompleted = currentAiReport != null,
                readiness = null,
                showAgeTransitionPrompt = shouldShowAgePrompt,
                summary = summary,
                showSummary = isAllAnswered,
                remainingQuestions = remaining,
                isMinAnswersRequirementMet = met
            )
        }
        child?.let {
            fetchReadiness(it.id, period.code)
        }
    }

    fun onAnswerChange(questionId: UUID, value: Double?) {
        if (value == null) {
            onAnswerCleared(questionId)
            _uiState.update { it.copy(showSummary = false) }
            return
        }
        val period = _uiState.value.currentPeriod ?: return
        val question = period.questions.find { it.id == questionId } ?: return
        val optionId = question.options.find { it.value == value }?.id ?: return
        onAnswerSelected(questionId, optionId)
    }

    fun onToggleSummary(show: Boolean) {
        _uiState.update { it.copy(showSummary = show) }
    }

    fun onAnswerSelected(questionId: UUID, optionId: UUID) {
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
                val (remaining, met) = calculateRequirementMet(newAnswers, state.pendingAnswers, state.currentPeriod)
                val summary = state.currentPeriod?.questions?.toSummary(newAnswers) ?: state.summary
                val isAllAnswered = summary.completed == summary.totalQuestions && summary.totalQuestions > 0
                state.copy(
                    answers = newAnswers,
                    summary = summary,
                    showSummary = state.showSummary || isAllAnswered,
                    remainingQuestions = remaining,
                    isMinAnswersRequirementMet = met
                )
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
            val newPending = state.pendingAnswers + (questionId to Answer(questionId, optionId))
            val (remaining, met) = calculateRequirementMet(newAnswers, newPending, state.currentPeriod)
            val summary = state.currentPeriod?.questions?.toSummary(newAnswers) ?: state.summary
            val isAllAnswered = summary.completed == summary.totalQuestions && summary.totalQuestions > 0
            state.copy(
                answers = newAnswers,
                pendingAnswers = newPending,
                summary = summary,
                showSummary = state.showSummary || isAllAnswered,
                remainingQuestions = remaining,
                isMinAnswersRequirementMet = met
            )
        }
        _saveTrigger.tryEmit(Unit)
    }

    fun onAnswerCleared(questionId: UUID) {
        _uiState.update { state ->
            val newAnswers = state.answers.map {
                if (it.questionId == questionId) it.copy(option = null) else it
            }
            val (remaining, met) = calculateRequirementMet(newAnswers, state.pendingAnswers, state.currentPeriod)
            val summary = state.currentPeriod?.questions?.toSummary(newAnswers) ?: state.summary
            state.copy(
                answers = newAnswers,
                summary = summary,
                showSummary = false,
                remainingQuestions = remaining,
                isMinAnswersRequirementMet = met
            )
        }
    }

    private fun calculateRequirementMet(
        answers: List<AnswerUI>,
        pendingAnswers: Map<UUID, Answer>,
        period: LifePeriod?
    ): Pair<Int, Boolean> {
        val totalQuestions = period?.questions?.size ?: 0
        if (totalQuestions == 0) return 0 to true
        val periodQuestionIds = period?.questions?.map { it.id }?.toSet() ?: emptySet()
        val answeredCount = answers.count {
            it.questionId in periodQuestionIds &&
            it.option != null &&
            (it.assessmentId != null || pendingAnswers.containsKey(it.questionId))
        }
        val targetCount = ceil(totalQuestions * 0.8).toInt()
        val remaining = max(0, targetCount - answeredCount)
        return remaining to (remaining == 0)
    }

    fun completePeriod(allowEarlyTransition: Boolean = false) {
        val childId = selectedChild.value?.id ?: return
        val period = _uiState.value.currentPeriod ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    showEarlyTransitionDialog = false
                )
            }

            // Final Guard: Check readiness one last time before actual completion
            val readinessResult = getPeriodReadinessUseCase(childId, period.code)
            val readiness = readinessResult.getOrNull()

            if (readiness != null) {
                _uiState.update { it.copy(readiness = readiness) }
                if (!readiness.isReady && !allowEarlyTransition) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showEarlyTransitionDialog = true,
                            error = DomainError.UnknownError(readiness.reasons.joinToString(", "))
                        )
                    }
                    return@launch
                }
            }

            // Final save if pending
            val state = _uiState.value
            state.pendingAnswers.forEach { (qId, pending) ->
                saveAssessmentDraftUseCase(
                    periodId = period.id,
                    childId = childId,
                    skillAssessmentId = state.answers.find { it.questionId == qId }?.assessmentId,
                    answer = pending
                )
            }

            val result = completeLifePeriodUseCase(childId, period.code, allowEarlyTransition)

            if (result.isSuccess) {
                val completionResult = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCompleted = true,
                        completionResult = completionResult
                    )
                }
                val reportId = completionResult.reportId
                if (reportId != null) {
                    waitForReport(
                        reportId = reportId,
                        childId = childId
                    )
                }
            } else {
                val error = result.exceptionOrNull()
                val domainError = (error as? DomainError) ?: DomainError.UnknownError(error?.message ?: "Unknown error", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = domainError
                    )
                }
            }
        }
    }

    private fun waitForReport(reportId: UUID, childId: UUID) {
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

    fun dismissAgeTransitionPrompt() {
        _uiState.update { it.copy(showAgeTransitionPrompt = false) }
    }

    fun refresh() {
        val child = selectedChild.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val periodsRes = syncLifePeriodsUseCase()
            val assessmentsRes = syncChildAssessmentsUseCase(childId = child.id)
            val reportsRes = syncChildAiReportsUseCase(childId = child.id)

            val firstFailure = periodsRes.exceptionOrNull()
                ?: assessmentsRes.exceptionOrNull()
                ?: reportsRes.exceptionOrNull()

            if (firstFailure != null) {
                Log.e(TAG, "Refresh error", firstFailure)
                _uiState.update { it.copy(error = DomainError.fromThrowable(firstFailure), isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private companion object {
        private const val TAG = "DevelopmentVM"
    }
}
