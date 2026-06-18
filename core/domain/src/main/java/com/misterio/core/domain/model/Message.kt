package com.misterio.core.domain.model

import java.util.UUID

data class Message(
    val id: UUID = UUID.randomUUID(),
    val text: String,
    val isFromUser: Boolean,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
