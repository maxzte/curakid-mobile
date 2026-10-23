package com.misterio.curakid.feature.chat

import com.misterio.core.domain.model.ChatMessage
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.domain.usecase.GetChildUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val getChildUseCase: GetChildUseCase = mockk()

    private val testChild = Child(
        id = UUID.randomUUID(),
        name = "Іванко",
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
    fun `init loads chat history for selected child`() = runTest(testDispatcher) {
        val historyMsg = ChatMessage(
            id = UUID.randomUUID(),
            childId = testChild.id,
            userText = "Як справи?",
            aiText = "Все добре!",
            isStreaming = false,
            createdAt = "2026-10-14T10:00:00Z"
        )
        every { getChildUseCase() } returns flowOf(testChild)
        coEvery { chatRepository.getChatHistory(testChild.id) } returns Result.success(listOf(historyMsg))

        val viewModel = ChatViewModel(chatRepository, getChildUseCase, testDispatcher)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertEquals(2, viewModel.messages.value.size)
        assertEquals("Як справи?", viewModel.messages.value[0].userText)
        assertEquals("Все добре!", viewModel.messages.value[1].aiText)
    }

    @Test
    fun `sendMessage appends user message and streams ai response`() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(testChild)
        coEvery { chatRepository.getChatHistory(testChild.id) } returns Result.success(emptyList())
        every { chatRepository.startChatStream(any(), testChild.id, "Привіт") } returns flow {
            emit("При")
            emit("віт!")
        }

        val viewModel = ChatViewModel(chatRepository, getChildUseCase, testDispatcher)
        advanceUntilIdle()

        viewModel.sendMessage("Привіт")
        advanceUntilIdle()

        val messages = viewModel.messages.value
        assertTrue(messages.any { it.userText == "Привіт" })
        assertTrue(messages.any { it.aiText == "Привіт!" })
    }

    @Test
    fun `sendMessage handles error gracefully`() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(testChild)
        coEvery { chatRepository.getChatHistory(testChild.id) } returns Result.success(emptyList())
        every { chatRepository.startChatStream(any(), testChild.id, "Test") } returns flow {
            throw RuntimeException("Network timeout")
        }

        val viewModel = ChatViewModel(chatRepository, getChildUseCase, testDispatcher)
        advanceUntilIdle()

        viewModel.sendMessage("Test")
        advanceUntilIdle()

        val messages = viewModel.messages.value
        assertTrue(messages.any { it.userText == "Test" })
        assertTrue(messages.any { it.aiText.contains("Помилка") })
    }
}
