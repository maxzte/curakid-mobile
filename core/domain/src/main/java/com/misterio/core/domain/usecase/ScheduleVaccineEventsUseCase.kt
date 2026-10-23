package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.VaccineRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class ScheduleVaccineEventsUseCase @Inject constructor(
    private val vaccineRepository: VaccineRepository,
) {
    suspend operator fun invoke(childId: UUID, childBirthDate: LocalDate): Result<Unit> {
        return vaccineRepository.syncVaccines(childId)
    }
}
