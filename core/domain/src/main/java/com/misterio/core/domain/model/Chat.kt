package com.misterio.core.domain.model

import java.util.UUID

data class ChatMessage(
    val id: UUID,
    val childId: UUID,
    val userText: String,
    val aiText: String,
    val isStreaming: Boolean,
    val createdAt: String,
)
