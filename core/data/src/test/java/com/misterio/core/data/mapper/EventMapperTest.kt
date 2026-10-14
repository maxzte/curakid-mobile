package com.misterio.core.data.mapper

import com.misterio.core.database.entity.EventEntity
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.VaccineType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.util.UUID

class EventMapperTest {

    @Test
    fun `toDomain correctly maps standard vaccine event`() {
        val now = LocalDateTime.now()
        val entity = EventEntity(
            id = UUID.randomUUID(),
            childId = UUID.randomUUID(),
            title = "Гепатит В",
            type = "VACCINE",
            date = now,
            description = "Вакцинація перша доза",
            status = "PLANNED",
            isSynced = true,
            disease = "Гепатит В",
            doseNumber = 1,
            totalDoses = 3,
            vaccineType = "VACCINATION",
            scheduledAgeMonths = 0,
            scheduledAgeDays = 1,
            notes = null
        )

        val domain = entity.toDomain()

        assertTrue(domain is Event.Vaccine)
        val vaccine = domain as Event.Vaccine
        assertEquals("Гепатит В", vaccine.title)
        assertEquals(EventType.VACCINE, vaccine.type)
        assertEquals(EventStatus.PLANNED, vaccine.status)
        assertEquals(VaccineType.VACCINATION, vaccine.vaccineType)
    }

    @Test
    fun `toDomain falls back to default safely when backend sends unknown type or status`() {
        val entity = EventEntity(
            id = UUID.randomUUID(),
            childId = UUID.randomUUID(),
            title = "Огляд",
            type = "UNKNOWN_NEW_TYPE",
            date = LocalDateTime.now(),
            description = "Опис",
            status = "UNKNOWN_STATUS",
            isSynced = false
        )

        // Must not throw IllegalArgumentException
        val domain = entity.toDomain()

        assertTrue(domain is Event.General)
        val general = domain as Event.General
        assertEquals(EventType.USER, general.type)
        assertEquals(EventStatus.PLANNED, general.status)
    }
}
