package com.misterio.core.data.mapper

import com.misterio.core.network.dto.ChatMessageResponse
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatMapperTest {

    @Test
    fun `ChatMessageResponse toDomain correctly maps all fields`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val dto = ChatMessageResponse(
            id = id,
            childId = childId,
            userText = "Is 38C fever high?",
            aiText = "Consult a pediatrician if fever persists.",
            createdAt = "2025-01-01T12:00:00Z",
            urgencyLevel = "MEDIUM",
            verifiedSources = listOf("WHO Guidelines")
        )

        val domain = dto.toDomain()

        assertEquals(id, domain.id)
        assertEquals(childId, domain.childId)
        assertEquals("Is 38C fever high?", domain.userText)
        assertEquals("Consult a pediatrician if fever persists.", domain.aiText)
        assertFalse(domain.isStreaming)
        assertEquals("2025-01-01T12:00:00Z", domain.createdAt)
        assertEquals("MEDIUM", domain.urgencyLevel)
        assertEquals(listOf("WHO Guidelines"), domain.verifiedSources)
    }
}
