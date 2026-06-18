package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.repository.EventRepository
import java.util.UUID
import javax.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(childId: UUID, event: Event): Result<Unit> =
        repository.updateEvent(childId, event)
}
