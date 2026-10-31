package com.misterio.core.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AgeCalculatorTest {

    @Test
    fun calculateAgeInMonths_calculatesCorrectly() {
        val birthDate = LocalDate.of(2025, 1, 15)
        val targetDate = LocalDate.of(2025, 7, 15)
        assertEquals(6, AgeCalculator.calculateAgeInMonths(birthDate, targetDate))
    }

    @Test
    fun calculateAgeInDays_calculatesCorrectly() {
        val birthDate = LocalDate.of(2025, 1, 1)
        val targetDate = LocalDate.of(2025, 1, 11)
        assertEquals(10L, AgeCalculator.calculateAgeInDays(birthDate, targetDate))
    }

    @Test
    fun calculateDetailedAge_calculatesMonthsAndRemainingDays() {
        val birthDate = LocalDate.of(2025, 1, 1)
        val targetDate = LocalDate.of(2025, 3, 20) // 78 days
        val (months, days) = AgeCalculator.calculateDetailedAge(birthDate, targetDate)
        assertEquals(2, months)
        assertEquals(18, days)
    }

    @Test
    fun calculateEffectiveBirthDate_prematureInfantAdjustsUntil24Months() {
        // Child born at 32 weeks (8 weeks premature -> 56 days)
        val birthDate = LocalDate.of(2024, 1, 1)
        val under2Years = LocalDate.of(2024, 6, 1)
        val effectiveDate = AgeCalculator.calculateEffectiveBirthDate(
            birthDate = birthDate,
            isPremature = true,
            gestationalWeeks = 32,
            currentDate = under2Years
        )
        assertEquals(birthDate.plusDays(56), effectiveDate)

        // After 24 months, correction is no longer applied
        val over2Years = LocalDate.of(2026, 4, 1)
        val effectiveDateAfter2Years = AgeCalculator.calculateEffectiveBirthDate(
            birthDate = birthDate,
            isPremature = true,
            gestationalWeeks = 32,
            currentDate = over2Years
        )
        assertEquals(birthDate, effectiveDateAfter2Years)
    }

    @Test
    fun calculateEffectiveBirthDate_fullTermChildUnchanged() {
        val birthDate = LocalDate.of(2024, 1, 1)
        val effectiveDate = AgeCalculator.calculateEffectiveBirthDate(
            birthDate = birthDate,
            isPremature = false,
            gestationalWeeks = null
        )
        assertEquals(birthDate, effectiveDate)
    }
}
