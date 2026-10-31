package com.misterio.core.domain.repository

import com.misterio.core.domain.model.ChatMessage
import com.misterio.core.domain.model.ChatStreamEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ChatRepository {

    fun getChatMessages(childId: UUID): Flow<List<ChatMessage>>

    suspend fun syncChatHistory(childId: UUID): Result<Unit>

    suspend fun saveMessage(message: ChatMessage)

    fun startChatStream(id: UUID, childId: UUID, text: String): Flow<ChatStreamEvent>

}
