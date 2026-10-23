package com.misterio.core.data.mapper

import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.domain.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class ChildMapperTest {

    @Test
    fun `toDomain correctly maps known gender`() {
        val entity = ChildEntity(
            id = UUID.randomUUID(),
            name = "Софійка",
            birthDate = LocalDate.of(2023, 5, 10),
            gender = "FEMALE",
            isPremature = false,
            gestationalWeeks = 40,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        val child = entity.toDomain()

        assertEquals("Софійка", child.name)
        assertEquals(Gender.FEMALE, child.gender)
    }

    @Test
    fun `toDomain safely falls back to default gender when backend sends unknown string`() {
        val entity = ChildEntity(
            id = UUID.randomUUID(),
            name = "Олександр",
            birthDate = LocalDate.of(2022, 1, 1),
            gender = "UNKNOWN_NON_BINARY_OR_OTHER",
            isPremature = false,
            gestationalWeeks = null,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        // Must not crash with IllegalArgumentException
        val child = entity.toDomain()

        assertEquals("Олександр", child.name)
        assertEquals(Gender.MALE, child.gender)
    }

    @Test
    fun `toEntity preserves gestationalWeeks from ChildResponse and Child`() {
        val remoteDto = com.misterio.core.network.dto.ChildResponse(
            id = UUID.randomUUID(),
            name = "Лука",
            birthDate = "2023-01-15",
            gender = "MALE",
            isPremature = true,
            gestationalWeeks = 34,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        val entityFromDto = remoteDto.toEntity()
        assertEquals(34, entityFromDto.gestationalWeeks)
        assertEquals(true, entityFromDto.isPremature)

        val domainChild = entityFromDto.toDomain()
        assertEquals(34, domainChild.gestationalWeeks)

        val entityFromDomain = domainChild.toEntity()
        assertEquals(34, entityFromDomain.gestationalWeeks)
    }
}
