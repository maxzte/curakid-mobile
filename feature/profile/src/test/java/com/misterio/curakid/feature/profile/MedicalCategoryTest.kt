package com.misterio.curakid.feature.profile

import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class MedicalCategoryTest {

    private val sampleChild = Child(
        id = UUID.randomUUID(),
        name = "Тарас Шевченко",
        birthDate = LocalDate.of(2023, 1, 1),
        gender = Gender.MALE,
        isPremature = false,
        gestationalWeeks = null,
        allergies = listOf("Пеніцилін"),
        chronicDiseases = listOf("Астма"),
        pastDiseases = listOf("Вітрянка"),
        specialFeatures = listOf("Окуляри")
    )

    @Test
    fun `getMedicalItems returns expected list for category`() {
        assertEquals(listOf("Пеніцилін"), sampleChild.getMedicalItems(MedicalCategory.ALLERGIES))
        assertEquals(listOf("Астма"), sampleChild.getMedicalItems(MedicalCategory.CHRONIC))
        assertEquals(listOf("Вітрянка"), sampleChild.getMedicalItems(MedicalCategory.PAST))
        assertEquals(listOf("Окуляри"), sampleChild.getMedicalItems(MedicalCategory.FEATURES))
    }

    @Test
    fun `withMedicalItems updates expected list for category`() {
        val updatedAllergies = sampleChild.withMedicalItems(MedicalCategory.ALLERGIES, listOf("Арахіс", "Мед"))
        assertEquals(listOf("Арахіс", "Мед"), updatedAllergies.allergies)
        assertEquals(listOf("Астма"), updatedAllergies.chronicDiseases)

        val updatedChronic = sampleChild.withMedicalItems(MedicalCategory.CHRONIC, emptyList())
        assertEquals(emptyList<String>(), updatedChronic.chronicDiseases)
    }

    @Test
    fun `initials returns capitalized initials`() {
        assertEquals("ТШ", sampleChild.initials)

        val singleNameChild = sampleChild.copy(name = "Тарас")
        assertEquals("Т", singleNameChild.initials)
    }
}
