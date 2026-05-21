package com.misterio.curakid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Message
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.domain.repository.ChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
                text = "Привіт! Я твій ШІ-асистент CuraKid. Чим я можу допомогти вам сьогодні?",
                isFromUser = false
            )
        )
    )
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val userMessage = Message(text = text, isFromUser = true)
            _messages.update { it + userMessage }

            val childId = childRepository.getSelectedChild().firstOrNull()?.id
                ?: return@launch // Or handle error

            _isLoading.value = true
            
            val aiMessageId = UUID.randomUUID().toString()
            val initialAiMessage = Message(id = aiMessageId, text = "", isFromUser = false)
            _messages.update { it + initialAiMessage }

            var fullText = ""
            chatRepository.startChatStream(UUID.fromString(childId), text)
                .collect { chunk ->
                    fullText += chunk
                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == aiMessageId) msg.copy(text = fullText) else msg
                        }
                    }
                    _isLoading.value = false
                }
        }
    }
}
