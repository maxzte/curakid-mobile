package com.misterio.curakid.feature.profile

import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.usecase.AddChildUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetChildrenUseCase
import com.misterio.core.domain.usecase.LogoutUseCase
import com.misterio.core.domain.usecase.SelectChildUseCase
import com.misterio.core.domain.usecase.SyncChildrenUseCase
import com.misterio.core.domain.usecase.UpdateChildUseCase
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
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val syncChildrenUseCase: SyncChildrenUseCase = mockk(relaxed = true)
    private val getChildrenUseCase: GetChildrenUseCase = mockk()
    private val selectChildUseCase: SelectChildUseCase = mockk(relaxed = true)
    private val addChildUseCase: AddChildUseCase = mockk(relaxed = true)
    private val updateChildUseCase: UpdateChildUseCase = mockk(relaxed = true)
    private val getChildUseCase: GetChildUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)

    private val sampleChild1 = Child(
        id = UUID.randomUUID(),
        name = "Марія",
        gender = Gender.FEMALE,
        birthDate = LocalDate.now().minusYears(2),
        isPremature = false,
        gestationalWeeks = 39,
        allergies = emptyList(),
        chronicDiseases = emptyList(),
        pastDiseases = emptyList(),
        specialFeatures = emptyList()
    )

    private val sampleChild2 = Child(
        id = UUID.randomUUID(),
        name = "Тарас",
        gender = Gender.MALE,
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads children list and observes selected child`() = runTest(testDispatcher) {
        every { getChildrenUseCase() } returns flowOf(listOf(sampleChild1, sampleChild2))
        every { getChildUseCase() } returns flowOf(sampleChild1)

        val viewModel = ProfileViewModel(
            syncChildrenUseCase,
            getChildrenUseCase,
            selectChildUseCase,
            addChildUseCase,
            updateChildUseCase,
            getChildUseCase,
            logoutUseCase
        )

        val job1 = launch { viewModel.children.collect {} }
        val job2 = launch { viewModel.selectedChild.collect {} }
        advanceUntilIdle()

        assertEquals(2, viewModel.children.value.size)
        assertEquals(sampleChild1, viewModel.selectedChild.value)
        coVerify { syncChildrenUseCase(false) }

        job1.cancel()
        job2.cancel()
    }

    @Test
    fun `onChildSelected invokes selectChildUseCase`() = runTest(testDispatcher) {
        every { getChildrenUseCase() } returns flowOf(listOf(sampleChild1, sampleChild2))
        every { getChildUseCase() } returns flowOf(sampleChild1)

        val viewModel = ProfileViewModel(
            syncChildrenUseCase,
            getChildrenUseCase,
            selectChildUseCase,
            addChildUseCase,
            updateChildUseCase,
            getChildUseCase,
            logoutUseCase
        )
        advanceUntilIdle()

        viewModel.onChildSelected(sampleChild2.id)
        advanceUntilIdle()

        coVerify { selectChildUseCase(sampleChild2.id) }
    }

    @Test
    fun `logout invokes logoutUseCase`() = runTest(testDispatcher) {
        every { getChildrenUseCase() } returns flowOf(emptyList())
        every { getChildUseCase() } returns flowOf(null)

        val viewModel = ProfileViewModel(
            syncChildrenUseCase,
            getChildrenUseCase,
            selectChildUseCase,
            addChildUseCase,
            updateChildUseCase,
            getChildUseCase,
            logoutUseCase
        )
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        coVerify { logoutUseCase() }
    }
}
