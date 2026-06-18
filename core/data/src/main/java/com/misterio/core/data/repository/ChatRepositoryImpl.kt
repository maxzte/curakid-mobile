package com.misterio.core.data.repository

import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.domain.model.ChatMessage
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
        val json = """{"id":"$id","childId":"$childId","text":"$text"}"""
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${BASE_URL}api/chat/stream")
            .post(requestBody)
            .build()

        val factory = EventSources.createFactory(okHttpClient)

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                // можна перевірити response.code(), щоб упевнитись у 200 OK
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                trySend(data)   // кожен chunk – окремий рядок у Flow
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                close(t)
            }
        }

        val eventSource = factory.newEventSource(request, listener)

        awaitClose { eventSource.cancel() }
    }
}
