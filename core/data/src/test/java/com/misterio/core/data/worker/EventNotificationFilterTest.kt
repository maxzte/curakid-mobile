package com.misterio.core.data.worker

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.curakid.core.common.SettingsManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.UUID

class EventNotificationFilterTest {

    private val settingsManager: SettingsManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        coEvery { settingsManager.isNotificationsEnabled } returns flowOf(true)
    }

    @Test
    fun `completed or skipped events are filtered out from scheduling`() = runTest {
        val now = LocalDateTime.now()
        val plannedEvent = Event.General(
            id = UUID.randomUUID(),
            title = "Planned Visit",
            type = EventType.DOCTOR,
            date = now.plusDays(2),
            status = EventStatus.PLANNED,
            description = null
        )
        val doneEvent = plannedEvent.copy(
            id = UUID.randomUUID(),
            title = "Done Vaccine",
            status = EventStatus.DONE
        )
        val skippedEvent = plannedEvent.copy(
            id = UUID.randomUUID(),
            title = "Skipped Vaccine",
            status = EventStatus.SKIPPED
        )
        val pastPlannedEvent = plannedEvent.copy(
            id = UUID.randomUUID(),
            title = "Past Planned",
            date = now.minusDays(1)
        )

        val events = listOf(plannedEvent, doneEvent, skippedEvent, pastPlannedEvent)

        val validForScheduling = events.filter { event ->
            event.status == EventStatus.PLANNED && event.date.isAfter(now)
        }

        assertEquals(1, validForScheduling.size)
        assertEquals(plannedEvent.id, validForScheduling.first().id)
        assertTrue(validForScheduling.none { it.status == EventStatus.DONE })
        assertTrue(validForScheduling.none { it.status == EventStatus.SKIPPED })
    }
}
