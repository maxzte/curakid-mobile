package com.misterio.core.domain.repository

import com.misterio.core.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ChatRepository {

    suspend fun getChatHistory(childId: UUID): Result<List<ChatMessage>>

    fun startChatStream(id: UUID, childId: UUID, text: String): Flow<String>

}
