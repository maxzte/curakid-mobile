package com.misterio.curakid.feature.development

import app.cash.turbine.test
import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.AssessmentAnswer
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.model.DomainReadiness
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.model.Option
import com.misterio.core.domain.model.PeriodReadiness
import com.misterio.core.domain.model.Question
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class DevelopmentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getLifePeriodsUseCase: GetLifePeriodsUseCase = mockk()
    private val getChildAssessmentsUseCase: GetChildAssessmentsUseCase = mockk()
    private val syncLifePeriodsUseCase: SyncLifePeriodsUseCase = mockk(relaxed = true)
    private val syncChildAssessmentsUseCase: SyncChildAssessmentsUseCase = mockk(relaxed = true)
    private val completeLifePeriodUseCase: CompleteLifePeriodUseCase = mockk()
    private val getMeasurementsUseCase: GetMeasurementsUseCase = mockk()
    private val getChildUseCase: GetChildUseCase = mockk()
    private val saveAssessmentDraftUseCase: SaveAssessmentDraftUseCase = mockk()
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase = mockk()
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase = mockk(relaxed = true)
    private val pollReportStatusUseCase: PollReportStatusUseCase = mockk(relaxed = true)
    private val getPeriodReadinessUseCase: GetPeriodReadinessUseCase = mockk()

    private val testChildId = UUID.randomUUID()
    // Child is 2 months old (matches period 0-3 months)
    private val testChild = Child(
        id = testChildId,
        name = "Софійка",
        gender = Gender.FEMALE,
        birthDate = LocalDate.now().minusMonths(2),
        isPremature = false,
        gestationalWeeks = 40,
        allergies = emptyList(),
        chronicDiseases = emptyList(),
        pastDiseases = emptyList(),
        specialFeatures = emptyList()
    )

    private val questionId = UUID.randomUUID()
    private val optionYes = Option(id = UUID.randomUUID(), text = "Так", value = 1.0, displayOrder = 1)
    private val optionNo = Option(id = UUID.randomUUID(), text = "Ні", value = 0.0, displayOrder = 2)
    private val sampleQuestion = Question(
        id = questionId,
        text = "Тримає голівку",
        domain = "MOTOR",
        weight = 1.0,
        options = listOf(optionYes, optionNo)
    )

    private val period1Id = UUID.randomUUID()
    private val samplePeriod1 = LifePeriod(
        id = period1Id,
        code = "PERIOD_0_3",
        label = "0-3 місяці",
        about = null,
        minMonths = 0,
        maxMonths = 3,
        questions = listOf(sampleQuestion)
    )

    private val period2Id = UUID.randomUUID()
    private val samplePeriod2 = LifePeriod(
        id = period2Id,
        code = "PERIOD_3_6",
        label = "3-6 місяців",
        about = null,
        minMonths = 3,
        maxMonths = 6,
        questions = emptyList()
    )

    private val sampleAssessment = ChildAssessment(
        id = UUID.randomUUID(),
        childId = testChildId,
        answers = listOf(
            AssessmentAnswer(
                id = UUID.randomUUID(),
                question = sampleQuestion,
                selectedOption = optionYes,
                answeredAt = Clock.System.now()
            )
        ),
        assessmentDate = "2026-10-24"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { getChildUseCase() } returns flowOf(testChild)
        every { getLifePeriodsUseCase() } returns flowOf(listOf(samplePeriod1, samplePeriod2))
        every { getChildAssessmentsUseCase(testChildId) } returns flowOf(emptyList())
        every { getChildAiReportsUseCase(testChildId) } returns flowOf(emptyList())
        every { getMeasurementsUseCase(testChildId) } returns flowOf(emptyList())
        coEvery { getPeriodReadinessUseCase(testChildId, any()) } returns Result.success(
            PeriodReadiness(
                isReady = true,
                completionPercent = 100,
                requiredPercent = 80,
                domainStatus = listOf(
                    DomainReadiness(
                        domain = "MOTOR",
                        completionPercent = 100,
                        isReady = true,
                        requiredPercent = 80
                    )
                ),
                reasons = emptyList(),
                canTransitionEarly = false
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DevelopmentViewModel(
        getLifePeriodsUseCase = getLifePeriodsUseCase,
        getChildAssessmentsUseCase = getChildAssessmentsUseCase,
        syncLifePeriodsUseCase = syncLifePeriodsUseCase,
        syncChildAssessmentsUseCase = syncChildAssessmentsUseCase,
        completeLifePeriodUseCase = completeLifePeriodUseCase,
        getMeasurementsUseCase = getMeasurementsUseCase,
        getChildUseCase = getChildUseCase,
        saveAssessmentDraftUseCase = saveAssessmentDraftUseCase,
        getChildAiReportsUseCase = getChildAiReportsUseCase,
        syncChildAiReportsUseCase = syncChildAiReportsUseCase,
        pollReportStatusUseCase = pollReportStatusUseCase,
        getPeriodReadinessUseCase = getPeriodReadinessUseCase
    )

    @Test
    fun `uiState combines data sources and sets initial period correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            advanceUntilIdle()

            val loadedState = expectMostRecentItem()
            assertEquals(2, loadedState.lifePeriods.size)
            assertNotNull(loadedState.currentPeriod)
            assertEquals("PERIOD_0_3", loadedState.currentPeriod?.code)
            assertFalse(loadedState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { syncLifePeriodsUseCase() }
        coVerify { syncChildAssessmentsUseCase(testChildId) }
        coVerify { syncChildAiReportsUseCase(testChildId) }
    }

    @Test
    fun `selecting different period updates currentPeriod and fetches readiness`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectPeriod(samplePeriod2)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("PERIOD_3_6", state.currentPeriod?.code)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { getPeriodReadinessUseCase(testChildId, "PERIOD_3_6") }
    }

    @Test
    fun `onAnswerSelected triggers auto-save with debounce and updates pendingAnswers`() = runTest(testDispatcher) {
        coEvery {
            saveAssessmentDraftUseCase(
                periodId = any(),
                childId = any(),
                answer = any(),
                skillAssessmentId = any()
            )
        } returns Result.success(sampleAssessment)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Directly invoke onAnswerSelected for question and option
        viewModel.onAnswerSelected(questionId, optionYes.id)

        // Immediately pending answers contains the selected option
        assertTrue(viewModel.uiState.value.pendingAnswers.containsKey(questionId))

        // Before debounce duration (1000ms), useCase should NOT be called yet
        advanceTimeBy(500)
        coVerify(exactly = 0) { saveAssessmentDraftUseCase(any(), any(), any(), any()) }

        // Advance past debounce
        advanceTimeBy(600)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            saveAssessmentDraftUseCase(
                periodId = period1Id,
                childId = testChildId,
                answer = match { it.questionId == questionId && it.selectedOptionId == optionYes.id },
                skillAssessmentId = null
            )
        }

        // After successful save, pendingAnswers is cleared
        assertTrue(viewModel.uiState.value.pendingAnswers.isEmpty())
    }

    @Test
    fun `completePeriod with normal transition updates completionResult`() = runTest(testDispatcher) {
        every { getChildAssessmentsUseCase(testChildId) } returns flowOf(listOf(sampleAssessment))
        val expectedResult = CompletePeriodResult(
            periodCode = "PERIOD_0_3",
            completionPercent = 100,
            requiredPercent = 80,
            completedAt = "2026-10-24T10:00:00Z",
            assessments = listOf(sampleAssessment),
            reportId = UUID.randomUUID()
        )
        coEvery {
            completeLifePeriodUseCase(testChildId, "PERIOD_0_3", false)
        } returns Result.success(expectedResult)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completePeriod(allowEarlyTransition = false)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expectedResult, state.completionResult)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            completeLifePeriodUseCase(testChildId, "PERIOD_0_3", false)
        }
    }
}
