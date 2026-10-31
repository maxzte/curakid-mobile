package com.misterio.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PrematureChildAgeTest {

    @Test
    fun `full term child has identical birthDate and effectiveBirthDate`() {
        val birthDate = LocalDate.now().minusMonths(6)
        val child = Child(
            id = java.util.UUID.randomUUID(),
            name = "Тарас",
            birthDate = birthDate,
            gender = Gender.MALE,
            isPremature = false,
            gestationalWeeks = null,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        assertEquals(birthDate, child.effectiveBirthDate)
        assertEquals(6, child.ageInMonths)
        assertEquals(child.chronologicalAgeInMonths, child.ageInMonths)
    }

    @Test
    fun `premature child born at 32 weeks gets corrected age of plus 8 weeks`() {
        // Born 4 months (approx 120 days) ago at 32 weeks (8 weeks premature = 56 days shift)
        val birthDate = LocalDate.now().minusWeeks(16)
        val child = Child(
            id = java.util.UUID.randomUUID(),
            name = "Олена",
            birthDate = birthDate,
            gender = Gender.FEMALE,
            isPremature = true,
            gestationalWeeks = 32,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        // Effective birth date should be shifted forward by (40 - 32) * 7 = 56 days
        assertEquals(birthDate.plusDays(56), child.effectiveBirthDate)
        // Chronological age is 16 weeks (approx 3-4 months), corrected age is 8 weeks (approx 1-2 months)
        val expectedCorrectedMonths = java.time.Period.between(child.effectiveBirthDate, LocalDate.now()).toTotalMonths().toInt()
        assertEquals(expectedCorrectedMonths, child.ageInMonths)
    }

    @Test
    fun `premature correction stops after 24 months of age`() {
        val birthDate = LocalDate.now().minusMonths(26)
        val child = Child(
            id = java.util.UUID.randomUUID(),
            name = "Максим",
            birthDate = birthDate,
            gender = Gender.MALE,
            isPremature = true,
            gestationalWeeks = 30,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        // At 26 months, correction is no longer applied (effectiveBirthDate equals chronological birthDate)
        assertEquals(birthDate, child.effectiveBirthDate)
        assertEquals(26, child.ageInMonths)
    }
}
