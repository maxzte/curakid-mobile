package com.misterio.core.data.mapper

import com.misterio.core.domain.model.ConsentType
import com.misterio.core.network.dto.LegalDocumentResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class LegalDocumentMapperTest {

    @Test
    fun `LegalDocumentResponse toDomain correctly maps enum and properties`() {
        val dto = LegalDocumentResponse(
            type = "TERMS",
            version = "1.0",
            url = "https://example.com/terms",
            publishedAt = "2025-01-01T00:00:00Z"
        )

        val domain = dto.toDomain()

        assertEquals(ConsentType.TERMS, domain.type)
        assertEquals("1.0", domain.version)
        assertEquals("https://example.com/terms", domain.url)
        assertEquals("2025-01-01T00:00:00Z", domain.publishedAt)
    }
}
