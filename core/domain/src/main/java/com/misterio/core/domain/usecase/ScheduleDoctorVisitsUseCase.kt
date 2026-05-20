package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.util.EventNotificationScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ScheduleDoctorVisitsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val notificationScheduler: EventNotificationScheduler,
) {
    suspend operator fun invoke(childId: String): Result<Unit> {
        return try {
            // Sync events first to make sure we have the latest schedule
            eventRepository.syncEvents(childId)

            val events = eventRepository.getEvents(childId)
                .first()
                .filter { it.type == EventType.DOCTOR }
            notificationScheduler.schedule(events)

//            val today = LocalDateTime.now()

//            events.forEach { event ->
//                val eventDate = event.date
//
//                // schedule only future events or today's
//                if (eventDate.isAfter(today) || eventDate.toLocalDate()
//                        .isEqual(today.toLocalDate())
//                ) {
//                    val event = Event(
//                        id = UUID.randomUUID().toString(),
//                        title = "Візит до лікаря: ${event.title}",
//                        type = EventType.DOCTOR,
//                        date = eventDate,
//                        description = "Плановий візит до педіатра",
//                        status = event.status,
//                    )
//
//                    notificationScheduler.schedule(event)
//                }
//            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
