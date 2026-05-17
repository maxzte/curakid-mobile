package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.repository.EventRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ScheduleDoctorVisitsUseCase @Inject constructor(
    private val syncEventsUseCase: SyncEventsUseCase,
    private val eventRepository: EventRepository,
    private val addEventUseCase: AddEventUseCase,
) {
    suspend operator fun invoke(childId: String): Result<Unit> {
        return try {
            // Sync events first to make sure we have the latest schedule
            syncEventsUseCase(childId)

            val events = eventRepository.getEvents(childId).first().filter { it.type == EventType.DOCTOR }
            val today = LocalDateTime.now()

            events.forEach { event ->
                val eventDate = event.date

                // schedule only future events or today's
                if (eventDate.isAfter(today) || eventDate.toLocalDate()
                        .isEqual(today.toLocalDate())
                ) {
                    val event = Event(
                        id = UUID.randomUUID().toString(),
                        title = "Візит до лікаря: ${event.title}",
                        type = EventType.DOCTOR,
                        date = eventDate,
                        description = "Плановий візит до педіатра",
                        status = event.status,
                    )
                    addEventUseCase(childId, event)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
