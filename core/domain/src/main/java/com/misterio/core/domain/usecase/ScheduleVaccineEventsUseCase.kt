package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.VaccineRepository
import com.misterio.core.domain.util.EventNotificationScheduler
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class ScheduleVaccineEventsUseCase @Inject constructor(
    private val vaccineRepository: VaccineRepository,
    private val notificationScheduler: EventNotificationScheduler,
) {
    suspend operator fun invoke(childId: String, childBirthDate: LocalDate): Result<Unit> {
        return try {
            // Sync vaccines first to make sure we have the latest schedule
            vaccineRepository.syncVaccines(childId)

            val vaccines = vaccineRepository.getVaccines(childId).first()
            val today = LocalDateTime.now()

            vaccines.forEach { vaccine ->
                val ageMonths = vaccine.scheduledAgeMonths ?: 0
                val eventDate = childBirthDate.plusMonths(ageMonths.toLong()).atStartOfDay()

                // schedule only future events or today's
                if (eventDate.isAfter(today) || eventDate.toLocalDate()
                        .isEqual(today.toLocalDate())
                ) {
                    notificationScheduler.schedule(vaccine)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
