package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.EventRepository
import java.util.UUID
import javax.inject.Inject

class ScheduleDoctorVisitsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {
    suspend operator fun invoke(childId: UUID): Result<Unit> {
        return try {
            // Sync events first to make sure we have the latest schedule
            // EventRepository.syncEvents already handles notification scheduling
            eventRepository.syncEvents(childId)

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
