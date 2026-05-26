package com.misterio.curakid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Message
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.domain.repository.ChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val childRepository: ChildRepository
) : ViewModel() {

    private val _messages = MutableStateFlow(
        listOf(
            Message(
                id = UUID.randomUUID().toString(),
                text = "Привіт! Я твій ШІ-асистент CuraKid. Чим я можу допомогти вам сьогодні?",
                isFromUser = false
            ),
//            Message(
//                id = UUID.randomUUID().toString(),
//                text = "Привіт!",
//                isFromUser = true
//            )
        )
    )
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendTestMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {

            // USER MESSAGE
            val userMessage = Message(
                text = text,
                isFromUser = true
            )

            _messages.update { it + userMessage }

            _isLoading.value = true

            delay(1200) // показати typing indicator

            val aiMessageId = UUID.randomUUID().toString()

            val fakeResponse = """
        Це тестова потокова відповідь від ШІ асистента CuraKid.
        Зараз текст буде поступово з'являтися по слову,
        щоб перевірити анімацію стрімінгу повідомлень у чаті.
    """.trimIndent()

            val words = fakeResponse.split(" ")

            var currentText = ""

            words.forEachIndexed { index, word ->

                currentText += if (index == 0) {
                    word
                } else {
                    " $word"
                }

                if (index == 0) {
                    _messages.update {
                        it + Message(
                            id = aiMessageId,
                            text = currentText,
                            isFromUser = false,
                            isStreaming = true
                        )
                    }

                    _isLoading.value = false
                } else {

                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(text = currentText)
                            } else {
                                msg
                            }
                        }
                    }
                }

                delay(120)
            }

            _messages.update { list ->
                list.map { msg ->
                    if (msg.id == aiMessageId) {
                        msg.copy(isStreaming = false)
                    } else {
                        msg
                    }
                }
            }
        }
    }


    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val userMessage = Message(text = text, isFromUser = true)
            _messages.update { it + userMessage }

            val childId = childRepository.getSelectedChild().firstOrNull()?.id
                ?: return@launch

            _isLoading.value = true

            val aiMessageId = UUID.randomUUID().toString()
            var firstChunk = true
            var fullText = ""

            runCatching {
                chatRepository.startChatStream(UUID.fromString(childId), text)
                    .collect { chunk ->
                        if (firstChunk) {
                            firstChunk = false
                            _messages.update {
                                it + Message(
                                    id = aiMessageId,
                                    text = chunk,
                                    isFromUser = false,
                                    isStreaming = true
                                )
                            }
                            _isLoading.value = false
                            fullText = chunk
                        } else {
                            fullText += chunk
                            _messages.update { list ->
                                list.map { msg ->
                                    if (msg.id == aiMessageId) msg.copy(text = fullText) else msg
                                }
                            }
                        }
                    }
            }.onFailure { e ->
                _isLoading.value = false
                if (firstChunk) {
                    _messages.update {
                        it + Message(
                            id = aiMessageId,
                            text = "Помилка: ${e.message ?: "Не вдалося отримати відповідь"}",
                            isFromUser = false
                        )
                    }
                } else {
                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == aiMessageId) msg.copy(text = "$fullText\n[Помилка]") else msg
                        }
                    }
                }
            }

            _messages.update { list ->
                list.map { msg ->
                    if (msg.id == aiMessageId) msg.copy(isStreaming = false) else msg
                }
            }
        }
    }
}
