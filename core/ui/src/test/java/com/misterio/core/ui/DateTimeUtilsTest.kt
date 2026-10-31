package com.misterio.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class DateTimeUtilsTest {

    @Test
    fun shortDateFormat_formatsCorrectly() {
        val date = LocalDate.of(2026, 10, 26)
        assertEquals("26.10.2026", date.toShortDate())
    }

    @Test
    fun dayMonthFormat_formatsWithUkrainianMonth() {
        val date = LocalDate.of(2026, 10, 26)
        assertEquals("26 жовтня", date.toDayMonth())
    }

    @Test
    fun monthYearFormat_formatsCorrectly() {
        val ym = YearMonth.of(2026, 10)
        val date = LocalDate.of(2026, 10, 15)
        assertEquals("Жовтень 2026", ym.toMonthYear())
        assertEquals("Жовтень 2026", date.toMonthYear())
    }

    @Test
    fun compactDateTimeFormat_formatsCorrectly() {
        val dt = LocalDateTime.of(2026, 10, 26, 14, 30)
        assertEquals("26.10.2026 14:30", dt.toCompactFormatted())
    }

    @Test
    fun formatAgeDetailed_formatsVariousCombinations() {
        assertEquals("0 дн", DateTimeUtils.formatAgeDetailed(0))
        assertEquals("15 дн", DateTimeUtils.formatAgeDetailed(15))
        assertEquals("1 міс 15 дн", DateTimeUtils.formatAgeDetailed(45))
        assertEquals("2 міс", DateTimeUtils.formatAgeDetailed(2, 0))
        assertEquals("2 міс 5 дн", DateTimeUtils.formatAgeDetailed(2, 5))
    }
}
