package com.misterio.curakid.feature.chat

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.node.AstNode
import com.misterio.core.domain.model.ChatMessage
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.usecase.GetChildUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Immutable
class StableAstNode(val node: AstNode) {
    // Оптимізація: розбиваємо на ноди заздалегідь у фоновому потоці
    val nodes: List<AstNode> by lazy {
        val result = mutableListOf<AstNode>()
        var iter = node.links.firstChild
        while (iter != null) {
            result.add(iter)
            iter = iter.links.next
        }
        result.reversed() // Реверсуємо один раз тут
    }
}

@Immutable
data class ChatMessageUiModel(
    val id: UUID,
    val childId: UUID,
    val userText: String,
    val aiText: String,
    val isStreaming: Boolean,
    val createdAt: String,
    val parsedAstNode: StableAstNode? = null
)

fun ChatMessage.toUiModel(parsedAstNode: StableAstNode? = null) = ChatMessageUiModel(
    id = id,
    childId = childId,
    userText = userText,
    aiText = aiText,
    isStreaming = isStreaming,
    createdAt = createdAt,
    parsedAstNode = parsedAstNode
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val childRepository: ChildRepository,
    private val getChildUseCase: GetChildUseCase,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessageUiModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageUiModel>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val markdownParser = CommonmarkAstNodeParser()

    init {
        Log.d(TAG, "INIT")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val selectedChild = getChildUseCase().first()
                if (selectedChild == null) {
                    _isLoading.value = false
                    return@launch
                }
                val history = chatRepository.getChatHistory(selectedChild.id).getOrElse {
                    emptyList()
                }

                // Split turns into separate user and AI messages for the UI
                val splitMessages = mutableListOf<ChatMessageUiModel>()
                history.forEach { msg ->
                    if (msg.userText.isNotBlank()) {
                        splitMessages.add(msg.copy(aiText = "", id = UUID.randomUUID()).toUiModel())
                    }
                    if (msg.aiText.isNotBlank()) {
                        val aiMsg = msg.copy(userText = "", id = UUID.randomUUID())
                        val astNode = withContext(Dispatchers.Default) {
                            markdownParser.parse(aiMsg.aiText)
                        }
                        splitMessages.add(aiMsg.toUiModel(parsedAstNode = StableAstNode(astNode)))
                    }
                }
                _messages.value = splitMessages
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load history", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val childId = childRepository.getSelectedChild().firstOrNull()?.id
                ?: return@launch
            val now = System.currentTimeMillis().milliseconds.toIsoString()
            val userMessage = ChatMessage(
                id = UUID.randomUUID(),
                childId = childId,
                userText = text,
                aiText = "",
                isStreaming = false,
                createdAt = now
            )
            _messages.update { it + userMessage.toUiModel() }


            _isLoading.value = true

            val messageId = UUID.randomUUID()
            var firstChunk = true
            var fullText = ""

            runCatching {
                chatRepository.startChatStream(messageId, childId, text)
                    .collect { chunk ->
                        if (firstChunk) {
                            firstChunk = false
                            _messages.update {
                                it + ChatMessage(
                                    id = messageId,
                                    childId = childId,
                                    aiText = chunk,
                                    userText = "",
                                    isStreaming = true,
                                    createdAt = now,
                                ).toUiModel()
                            }
                            _isLoading.value = false
                            fullText = chunk
                        } else {
                            fullText += chunk
                            _messages.update { list ->
                                if (list.isNotEmpty() && list.last().id == messageId) {
                                    val last = list.last()
                                    list.dropLast(1) + last.copy(aiText = fullText)
                                } else {
                                    list.map { msg ->
                                        if (msg.id == messageId) msg.copy(aiText = fullText) else msg
                                    }
                                }
                            }
                        }
                    }
            }.onFailure { e ->
                _isLoading.value = false
                if (firstChunk) {
                    _messages.update {
                        it + ChatMessage(
                            id = messageId,
                            childId = childId,
                            aiText = "Помилка: ${e.message ?: "Не вдалося отримати відповідь"}",
                            userText = "",
                            isStreaming = false,
                            createdAt = now,
                        ).toUiModel()
                    }
                } else {
                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == messageId) msg.copy(aiText = "$fullText\n[Помилка]") else msg
                        }
                    }
                }
            }

            // ПАРСИНГ ПІСЛЯ ЗАВЕРШЕННЯ СТРІМІНГУ
            val finalAstNode = withContext(Dispatchers.Default) {
                markdownParser.parse(fullText)
            }

            _messages.update { list ->
                list.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(isStreaming = false, parsedAstNode = StableAstNode(finalAstNode))
                    } else {
                        msg
                    }
                }
            }
        }
    }

    private companion object {
        private const val TAG = "ChatVM"
    }

}
