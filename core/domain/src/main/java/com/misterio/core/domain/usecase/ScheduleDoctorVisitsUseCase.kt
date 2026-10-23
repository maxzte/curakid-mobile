package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.EventRepository
import java.util.UUID
import javax.inject.Inject

class ScheduleDoctorVisitsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {
    suspend operator fun invoke(childId: UUID): Result<Unit> {
        return eventRepository.syncEvents(childId)
    }
}
