package com.misterio.core.data.mapper

import com.misterio.core.domain.model.ChatMessage
import com.misterio.core.network.dto.ChatMessageResponse

internal fun ChatMessageResponse.toDomain() = ChatMessage(
    id = id,
    childId = childId,
    userText = userText,
    aiText = aiText,
    isStreaming = false,
    createdAt = createdAt
)
