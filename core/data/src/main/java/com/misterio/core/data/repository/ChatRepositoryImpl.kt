package com.misterio.core.data.repository

import org.json.JSONObject
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.ChatDao
import com.misterio.core.database.entity.ChatMessageEntity
import com.misterio.core.domain.model.ChatMessage
import com.misterio.core.domain.model.ChatStreamEvent
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.network.di.BASE_URL
import com.misterio.core.network.di.SseClient
import com.misterio.core.network.api.ChatApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    private val chatDao: ChatDao,
    @param:SseClient private val okHttpClient: OkHttpClient,
) : ChatRepository {

    override fun getChatMessages(childId: UUID): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForChild(childId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    childId = entity.childId,
                    userText = entity.userText,
                    aiText = entity.aiText,
                    createdAt = entity.createdAt,
                    isStreaming = entity.isStreaming,
                    urgencyLevel = entity.urgencyLevel,
                    verifiedSources = entity.verifiedSources
                )
            }
        }
    }

    override suspend fun syncChatHistory(childId: UUID): Result<Unit> {
        return safeApiCall {
            chatApi.getChatHistory(childId)
        }.map { page ->
            val dtos = page.content
            val entities = dtos.map { dto ->
                ChatMessageEntity(
                    id = dto.id,
                    childId = dto.childId,
                    userText = dto.userText,
                    aiText = dto.aiText,
                    createdAt = dto.createdAt,
                    isStreaming = false,
                    hasError = false,
                    urgencyLevel = dto.urgencyLevel,
                    verifiedSources = dto.verifiedSources
                )
            }
            chatDao.insertMessages(entities)
        }
    }

    override suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(
            ChatMessageEntity(
                id = message.id,
                childId = message.childId,
                userText = message.userText,
                aiText = message.aiText,
                createdAt = message.createdAt,
                isStreaming = message.isStreaming,
                hasError = false,
                urgencyLevel = message.urgencyLevel,
                verifiedSources = message.verifiedSources
            )
        )
    }

    override fun startChatStream(
        id: UUID,
        childId: UUID,
        text: String,
    ): Flow<ChatStreamEvent> = callbackFlow {
        val json = JSONObject().apply {
            put("id", id.toString())
            put("childId", childId.toString())
            put("text", text)
        }.toString()
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${BASE_URL}api/v1/chat/stream")
            .post(requestBody)
            .build()

        val factory = EventSources.createFactory(okHttpClient)

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                // Connection opened
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (type == "metadata") {
                    try {
                        val metaObj = JSONObject(data)
                        val urgency = metaObj.optString("urgencyLevel", "ROUTINE")
                        val intent = metaObj.optString("intent", "SYMPTOM_OR_ILLNESS")
                        val status = metaObj.optString("statusMessage", "Звіряю з медичними протоколами ВООЗ...")
                        val sourcesArr = metaObj.optJSONArray("verifiedSources")
                        val sources = mutableListOf<String>()
                        if (sourcesArr != null) {
                            for (i in 0 until sourcesArr.length()) {
                                sources.add(sourcesArr.getString(i))
                            }
                        }
                        val promptsArr = metaObj.optJSONArray("suggestedPrompts")
                        val prompts = mutableListOf<String>()
                        if (promptsArr != null) {
                            for (i in 0 until promptsArr.length()) {
                                prompts.add(promptsArr.getString(i))
                            }
                        }
                        trySend(
                            ChatStreamEvent.Metadata(
                                urgencyLevel = urgency,
                                intent = intent,
                                statusMessage = status,
                                verifiedSources = sources,
                                suggestedPrompts = prompts
                            )
                        )
                    } catch (e: Exception) {
                        // ignore malformed metadata
                    }
                } else {
                    trySend(ChatStreamEvent.Chunk(data))
                }
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val error = when {
                    t != null -> t
                    response != null -> when (response.code) {
                        400 -> DomainError.BadRequestError("Помилка запиту: ${response.code}")
                        401 -> DomainError.AuthenticationError("Помилка авторизації: ${response.code}")
                        404 -> DomainError.NotFoundError("Чат не знайдено: ${response.code}")
                        in 500..599 -> DomainError.ServerError("Помилка сервера: ${response.code}")
                        else -> DomainError.UnknownError("Помилка з'єднання: ${response.code}")
                    }
                    else -> DomainError.UnknownError("Невідома помилка стрімінгу")
                }
                close(error)
            }
        }

        val eventSource = factory.newEventSource(request, listener)

        awaitClose { eventSource.cancel() }
    }
}
