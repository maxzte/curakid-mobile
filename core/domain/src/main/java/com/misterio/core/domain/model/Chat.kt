package com.misterio.core.domain.model

import java.util.UUID

data class ChatMessage(
    val id: UUID,
    val childId: UUID,
    val userText: String,
    val aiText: String,
    val isStreaming: Boolean,
    val createdAt: String,
    val urgencyLevel: String = "ROUTINE",
    val verifiedSources: List<String> = emptyList(),
)

sealed interface ChatStreamEvent {
    data class Metadata(
        val urgencyLevel: String = "ROUTINE",
        val intent: String = "SYMPTOM_OR_ILLNESS",
        val statusMessage: String = "Звіряю з медичними протоколами ВООЗ...",
        val verifiedSources: List<String> = emptyList(),
        val suggestedPrompts: List<String> = emptyList()
    ) : ChatStreamEvent

    data class Chunk(val text: String) : ChatStreamEvent
}
