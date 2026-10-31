package com.misterio.curakid.feature.profile

import app.cash.turbine.test
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.usecase.AddChildUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AddChildViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val addChildUseCase: AddChildUseCase = mockk()
    private lateinit var viewModel: AddChildViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddChildViewModel(addChildUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `formState updates correctly on input changes and calculates validity`() = runTest(testDispatcher) {
        viewModel.formState.test {
            val initial = awaitItem()
            assertFalse(initial.isFormValid)
            assertEquals("", initial.name)

            viewModel.onNameChange("Марія")
            val withName = awaitItem()
            assertEquals("Марія", withName.name)
            assertFalse(withName.isFormValid)

            val birthMillis = LocalDate.now().minusMonths(6)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            viewModel.onBirthDateChange(birthMillis)
            val withDate = awaitItem()
            assertEquals(birthMillis, withDate.birthDateMillis)
            assertTrue(withDate.isFormValid)

            viewModel.onGenderChange(Gender.FEMALE)
            val withGender = awaitItem()
            assertEquals(Gender.FEMALE, withGender.gender)

            viewModel.onPrematureChange(true)
            val withPremature = awaitItem()
            assertTrue(withPremature.isPremature)

            viewModel.onGestationalWeeksChange(34)
            val withWeeks = awaitItem()
            assertEquals(34, withWeeks.gestationalWeeks)
            assertTrue(withWeeks.isFormValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submit when form is invalid does not call useCase and keeps Initial state`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(UiState.Initial, awaitItem())

            viewModel.submit()
            advanceUntilIdle()

            coVerify(exactly = 0) {
                addChildUseCase(
                    any(), any(), any(), any(), any(), any(), any(), any(), any()
                )
            }
            expectNoEvents()
        }
    }

    @Test
    fun `submit when success emits Loading then Success and sends navigationEvent`() = runTest(testDispatcher) {
        val birthMillis = LocalDate.now().minusYears(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModel.onNameChange("Артем")
        viewModel.onBirthDateChange(birthMillis)
        viewModel.onGenderChange(Gender.MALE)

        coEvery {
            addChildUseCase(
                name = any(),
                birthDate = any(),
                gender = any(),
                isPremature = any(),
                gestationalWeeks = any(),
                allergies = any(),
                chronicDiseases = any(),
                pastDiseases = any(),
                specialFeatures = any()
            )
        } returns Result.success(Unit)

        viewModel.navigationEvent.test {
            viewModel.uiState.test {
                assertEquals(UiState.Initial, awaitItem())

                viewModel.submit()

                assertEquals(UiState.Loading, awaitItem())
                advanceUntilIdle()

                assertTrue(awaitItem() is UiState.Success)
            }

            assertEquals(Unit, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            addChildUseCase(
                name = "Артем",
                birthDate = birthMillis,
                gender = Gender.MALE,
                isPremature = false,
                gestationalWeeks = null,
                allergies = emptyList(),
                chronicDiseases = emptyList(),
                pastDiseases = emptyList(),
                specialFeatures = emptyList()
            )
        }
    }

    @Test
    fun `submit when failure emits Loading then Error`() = runTest(testDispatcher) {
        val birthMillis = LocalDate.now().minusYears(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModel.onNameChange("Артем")
        viewModel.onBirthDateChange(birthMillis)

        coEvery {
            addChildUseCase(
                name = any(),
                birthDate = any(),
                gender = any(),
                isPremature = any(),
                gestationalWeeks = any(),
                allergies = any(),
                chronicDiseases = any(),
                pastDiseases = any(),
                specialFeatures = any()
            )
        } returns Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(UiState.Initial, awaitItem())

            viewModel.submit()

            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is UiState.Error)
        }

        coVerify(exactly = 1) {
            addChildUseCase(
                name = "Артем",
                birthDate = birthMillis,
                gender = Gender.MALE,
                isPremature = false,
                gestationalWeeks = null,
                allergies = emptyList(),
                chronicDiseases = emptyList(),
                pastDiseases = emptyList(),
                specialFeatures = emptyList()
            )
        }
    }
}
