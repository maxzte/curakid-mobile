package com.misterio.core.network.dto

import java.util.UUID

data class ChatRequest(
    val id: UUID,
    val childId: UUID,
    val text: String
)

data class ChatMessageResponse(
    val id: UUID,
    val childId: UUID,
    val userText: String,
    val aiText: String,
    val createdAt: String,
)
