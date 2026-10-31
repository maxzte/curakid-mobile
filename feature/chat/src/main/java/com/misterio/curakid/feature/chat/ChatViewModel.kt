@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.chat

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.node.AstNode
import com.misterio.core.domain.model.ChatMessage
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.util.suspendRunCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

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
    val urgencyLevel: String = "ROUTINE",
    val verifiedSources: List<String> = emptyList(),
    val parsedAstNode: StableAstNode? = null
)

fun ChatMessage.toUiModel(parsedAstNode: StableAstNode? = null) = ChatMessageUiModel(
    id = id,
    childId = childId,
    userText = userText,
    aiText = aiText,
    isStreaming = isStreaming,
    createdAt = createdAt,
    urgencyLevel = urgencyLevel,
    verifiedSources = verifiedSources,
    parsedAstNode = parsedAstNode
)

@HiltViewModel
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val getChildUseCase: GetChildUseCase,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    @Inject
    constructor(
        chatRepository: ChatRepository,
        getChildUseCase: GetChildUseCase,
    ) : this(
        chatRepository = chatRepository,
        getChildUseCase = getChildUseCase,
        defaultDispatcher = Dispatchers.Default,
    )

    private val _messages = MutableStateFlow<List<ChatMessageUiModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageUiModel>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _streamingStatusText = MutableStateFlow<String?>(null)
    val streamingStatusText: StateFlow<String?> = _streamingStatusText.asStateFlow()

    private val _suggestedPrompts = MutableStateFlow<List<String>>(emptyList())
    val suggestedPrompts: StateFlow<List<String>> = _suggestedPrompts.asStateFlow()

    private val _activeRedFlag = MutableStateFlow(false)
    val activeRedFlag: StateFlow<Boolean> = _activeRedFlag.asStateFlow()

    private val markdownParser = CommonmarkAstNodeParser()

    private var streamingJob: kotlinx.coroutines.Job? = null

    init {
        Log.d(TAG, "INIT")
        viewModelScope.launch {
            getChildUseCase()
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collect { selectedChild ->
                    streamingJob?.cancel()
                    _isLoading.value = false
                    if (selectedChild == null) {
                        _messages.value = emptyList()
                        return@collect
                    }
                    _isLoading.value = true
                    suspendRunCatching {
                        chatRepository.syncChatHistory(selectedChild.id)
                    }
                    _isLoading.value = false
                    // Initial age-tailored starter prompts
                    _suggestedPrompts.value = listOf(
                        "Як оцінити розвиток за нормами ВООЗ?",
                        "Які поради щодо режиму дня та сну?",
                        "Коли вводити нові продукти в раціон?"
                    )
                }
        }

        viewModelScope.launch {
            getChildUseCase()
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .flatMapLatest { selectedChild ->
                    if (selectedChild == null) flowOf(emptyList())
                    else chatRepository.getChatMessages(selectedChild.id)
                }
                .collect { history ->
                    // Split turns into separate user and AI messages for the UI
                    val splitMessages = mutableListOf<ChatMessageUiModel>()
                    history.forEach { msg ->
                        if (msg.userText.isNotBlank()) {
                            val userMsgId = UUID.nameUUIDFromBytes("${msg.id}_user".toByteArray())
                            splitMessages.add(msg.copy(aiText = "", id = userMsgId).toUiModel())
                        }
                        if (msg.aiText.isNotBlank()) {
                            val aiMsgId = UUID.nameUUIDFromBytes("${msg.id}_ai".toByteArray())
                            val aiMsg = msg.copy(userText = "", id = aiMsgId)
                            val astNode = withContext(defaultDispatcher) {
                                markdownParser.parse(aiMsg.aiText)
                            }
                            splitMessages.add(aiMsg.toUiModel(parsedAstNode = StableAstNode(astNode)))
                        }
                    }
                    _messages.value = splitMessages
                }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        streamingJob?.cancel()
        streamingJob = viewModelScope.launch {
            val childId = getChildUseCase().first()?.id
                ?: return@launch
            val now = java.time.Instant.now().toString()
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
            _streamingStatusText.value = "Аналізую звернення через System 1..."
            _activeRedFlag.value = false

            val messageId = UUID.randomUUID()
            var firstChunk = true
            var fullText = ""
            var currentUrgency = "ROUTINE"
            var currentSources = emptyList<String>()

            suspendRunCatching {
                chatRepository.startChatStream(messageId, childId, text)
                    .collect { event ->
                        when (event) {
                            is com.misterio.core.domain.model.ChatStreamEvent.Metadata -> {
                                currentUrgency = event.urgencyLevel
                                currentSources = event.verifiedSources
                                _streamingStatusText.value = event.statusMessage
                                if (event.suggestedPrompts.isNotEmpty()) {
                                    _suggestedPrompts.value = event.suggestedPrompts
                                }
                                if (event.urgencyLevel == "URGENT_RED_FLAG") {
                                    _activeRedFlag.value = true
                                }
                            }
                            is com.misterio.core.domain.model.ChatStreamEvent.Chunk -> {
                                val chunk = event.text
                                if (firstChunk) {
                                    firstChunk = false
                                    _streamingStatusText.value = null
                                    _messages.update {
                                        it + ChatMessage(
                                            id = messageId,
                                            childId = childId,
                                            aiText = chunk,
                                            userText = "",
                                            isStreaming = true,
                                            createdAt = now,
                                            urgencyLevel = currentUrgency,
                                            verifiedSources = currentSources
                                        ).toUiModel()
                                    }
                                    _isLoading.value = false
                                    fullText = chunk
                                } else {
                                    fullText += chunk
                                    _messages.update { list ->
                                        if (list.isNotEmpty() && list.last().id == messageId) {
                                            val last = list.last()
                                            list.dropLast(1) + last.copy(
                                                aiText = fullText,
                                                urgencyLevel = currentUrgency,
                                                verifiedSources = currentSources
                                            )
                                        } else {
                                            list.map { msg ->
                                                if (msg.id == messageId) msg.copy(
                                                    aiText = fullText,
                                                    urgencyLevel = currentUrgency,
                                                    verifiedSources = currentSources
                                                ) else msg
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            }.onSuccess {
                _streamingStatusText.value = null
                _activeRedFlag.value = false
                // ПАРСИНГ ТІЛЬКИ ПРИ УСПІШНОМУ СТРІМІНГУ З НЕПОРОЖНІМ ТЕКСТОМ
                if (fullText.isNotBlank()) {
                    val finalAstNode = withContext(defaultDispatcher) {
                        markdownParser.parse(fullText)
                    }

                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == messageId) {
                                msg.copy(
                                    isStreaming = false,
                                    parsedAstNode = StableAstNode(finalAstNode),
                                    urgencyLevel = currentUrgency,
                                    verifiedSources = currentSources
                                )
                            } else {
                                msg
                            }
                        }
                    }

                    // Save turn to Room cache
                    chatRepository.saveMessage(
                        ChatMessage(
                            id = messageId,
                            childId = childId,
                            userText = text,
                            aiText = fullText,
                            createdAt = now,
                            isStreaming = false,
                            urgencyLevel = currentUrgency,
                            verifiedSources = currentSources
                        )
                    )
                }
            }.onFailure { e ->
                _isLoading.value = false
                _streamingStatusText.value = null
                _activeRedFlag.value = false
                if (firstChunk) {
                    _messages.update {
                        it + ChatMessage(
                            id = messageId,
                            childId = childId,
                            aiText = "Помилка: ${e.message ?: "Не вдалося отримати відповідь"}",
                            userText = "",
                            isStreaming = false,
                            createdAt = now,
                            urgencyLevel = currentUrgency,
                            verifiedSources = currentSources
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
        }
    }

    private companion object {
        private const val TAG = "ChatVM"
    }

}
