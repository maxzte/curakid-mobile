package com.misterio.core.domain.repository

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ChatRepository {
    fun startChatStream(childId: UUID, text: String): Flow<String>
}
