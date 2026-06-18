package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.VaccineRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class ScheduleVaccineEventsUseCase @Inject constructor(
    private val vaccineRepository: VaccineRepository,
) {
    suspend operator fun invoke(childId: UUID, childBirthDate: LocalDate): Result<Unit> {
        return try {
            // Sync vaccines first to make sure we have the latest schedule
            // Note: EventRepository.syncEvents already handles notification scheduling
            // during general sync or when vaccine events are fetched.
            vaccineRepository.syncVaccines(childId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
