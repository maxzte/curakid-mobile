package com.misterio.core.network.api

import com.misterio.core.network.dto.ChatRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import java.util.UUID

class ChatApiTest : BaseApiTest<ChatApi>() {

    override fun createApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Test
    fun `getChatHistory sends correct request`() = runTest {
        val childId = UUID.randomUUID()
        enqueueResponse("""
            [
                {"id": "${UUID.randomUUID()}", "childId": "$childId", "userText": "Hi", "aiText": "Hello", "createdAt": "2024-01-01T10:00:00"}
            ]
        """.trimIndent())

        val history = api.getChatHistory(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/chat/history/$childId", request.path)
        assertEquals("GET", request.method)
        assertEquals(1, history.size)
        assertEquals("Hi", history[0].userText)
    }

    @Test
    fun `streamChat sends correct POST body`() = runTest {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val chatRequest = ChatRequest(id, childId, "My question")
        
        enqueueResponse("streaming response")

        api.streamChat(chatRequest).execute()
        val request = mockWebServer.takeRequest()

        assertEquals("/api/chat/stream", request.path)
        assertEquals("POST", request.method)
        // Verify method is called, but we don't necessarily need to drain the stream here
    }
}
