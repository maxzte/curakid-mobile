package com.misterio.curakid.feature.development

import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.usecase.AddMeasurementUseCase
import com.misterio.core.domain.usecase.GetAiReportByIdUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetMeasurementNormsUseCase
import com.misterio.core.domain.usecase.GetMeasurementsUseCase
import com.misterio.core.domain.usecase.PollReportStatusUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ChildDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getMeasurementsUseCase: GetMeasurementsUseCase = mockk()
    private val addMeasurementUseCase: AddMeasurementUseCase = mockk(relaxed = true)
    private val getChildUseCase: GetChildUseCase = mockk()
    private val measurementRepository: MeasurementRepository = mockk(relaxed = true)
    private val pollReportStatusUseCase: PollReportStatusUseCase = mockk(relaxed = true)
    private val getAiReportByIdUseCase: GetAiReportByIdUseCase = mockk(relaxed = true)
    private val getMeasurementNormsUseCase: GetMeasurementNormsUseCase = mockk(relaxed = true)
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase = mockk(relaxed = true)

    private val sampleChild = Child(
        id = UUID.randomUUID(),
        name = "Марія",
        gender = Gender.FEMALE,
        birthDate = LocalDate.now().minusYears(1),
        isPremature = false,
        gestationalWeeks = 40,
        allergies = emptyList(),
        chronicDiseases = emptyList(),
        pastDiseases = emptyList(),
        specialFeatures = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getMeasurementNormsUseCase(any(), any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ChildDetailsViewModel {
        return ChildDetailsViewModel(
            getMeasurementsUseCase = getMeasurementsUseCase,
            addMeasurementUseCase = addMeasurementUseCase,
            getChildUseCase = getChildUseCase,
            measurementRepository = measurementRepository,
            pollReportStatusUseCase = pollReportStatusUseCase,
            getAiReportByIdUseCase = getAiReportByIdUseCase,
            getMeasurementNormsUseCase = getMeasurementNormsUseCase,
            syncChildAiReportsUseCase = syncChildAiReportsUseCase
        )
    }

    @Test
    fun `loads child and measurements successfully`() = runTest(testDispatcher) {
        val testMeasurement = Measurement(
            id = UUID.randomUUID(),
            childId = sampleChild.id,
            date = LocalDateTime.now(),
            aiReportId = null,
            weight = 10.5,
            height = 78.0,
            headCirc = 46.0,
            wfaZscore = 0.1,
            hfaZscore = 0.2,
            wfhZscore = -0.1,
            bmiZscore = 0.0,
            hcfaZscore = 0.3,
            wfaPercentile = 52.0,
            hfaPercentile = 55.0,
            wfhPercentile = 48.0,
            bmiPercentile = 50.0,
            hcfaPercentile = 58.0,
            isSynced = true
        )

        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getMeasurementsUseCase(sampleChild.id) } returns flowOf(listOf(testMeasurement))
        coEvery { getMeasurementNormsUseCase(any(), any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()
        val job1 = launch { viewModel.selectedChild.collect {} }
        val job2 = launch { viewModel.measurements.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.selectedChild.value is UiState.Success)
        assertEquals(sampleChild, (viewModel.selectedChild.value as UiState.Success).data)

        assertTrue(viewModel.measurements.value is UiState.Success)
        assertEquals(1, (viewModel.measurements.value as UiState.Success).data.size)

        coVerify { measurementRepository.syncMeasurementsForChild(sampleChild.id) }

        job1.cancel()
        job2.cancel()
    }

    @Test
    fun `addMeasurement invokes addMeasurementUseCase and syncs`() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getMeasurementsUseCase(sampleChild.id) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        val job = launch { viewModel.selectedChild.collect {} }
        advanceUntilIdle()

        viewModel.addMeasurement(weight = 11.0, height = 80.0, headCirc = 47.0)
        advanceUntilIdle()

        coVerify { addMeasurementUseCase(sampleChild.id, 11.0, 80.0, 47.0) }
        coVerify { measurementRepository.syncMeasurementsForChild(sampleChild.id) }

        job.cancel()
    }

    @Test
    fun `onChartTypeChange updates selected chart type`() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getMeasurementsUseCase(sampleChild.id) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(GrowthChartType.WeightForAge, viewModel.selectedChartType.value)

        viewModel.onChartTypeChange(GrowthChartType.HeightForAge)
        assertEquals(GrowthChartType.HeightForAge, viewModel.selectedChartType.value)
    }
}
