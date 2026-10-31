package com.misterio.core.data.util

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DataMapperUtilsTest {

    @Test
    fun `toLocalDateTime parses ISO Instant string`() {
        val result = "2025-01-01T12:00:00Z".toLocalDateTime()
        assertNotNull(result)
        assertEquals(2025, result.year)
        assertEquals(1, result.monthValue)
        assertEquals(1, result.dayOfMonth)
    }

    @Test
    fun `toLocalDateTime parses OffsetDateTime string`() {
        val result = "2025-05-15T14:30:00+02:00".toLocalDateTime()
        assertNotNull(result)
        assertEquals(2025, result.year)
        assertEquals(5, result.monthValue)
        assertEquals(15, result.dayOfMonth)
    }

    @Test
    fun `toLocalDateTime parses plain LocalDateTime string`() {
        val result = "2025-10-20T08:15:30".toLocalDateTime()
        assertEquals(LocalDateTime.of(2025, 10, 20, 8, 15, 30), result)
    }
}
