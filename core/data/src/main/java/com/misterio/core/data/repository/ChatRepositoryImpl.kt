package com.misterio.core.data.repository

import org.json.JSONObject
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.domain.model.ChatMessage
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.network.di.BASE_URL
import com.misterio.core.network.di.SseClient
import com.misterio.core.network.api.ChatApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
    @param:SseClient private val okHttpClient: OkHttpClient,
) : ChatRepository {

    override suspend fun getChatHistory(childId: UUID): Result<List<ChatMessage>> {
        return safeApiCall {
            chatApi.getChatHistory(childId)
        }.map { body ->
            body.map { it.toDomain() }
        }
    }

    override fun startChatStream(
        id: UUID,
        childId: UUID,
        text: String,
    ): Flow<String> = callbackFlow {
        val json = JSONObject().apply {
            put("id", id.toString())
            put("childId", childId.toString())
            put("text", text)
        }.toString()
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${BASE_URL}api/chat/stream")
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
                trySend(data)   // Each chunk is emitted to Flow
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
