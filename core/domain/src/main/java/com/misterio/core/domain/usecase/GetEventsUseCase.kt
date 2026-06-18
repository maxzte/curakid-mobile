package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.EventRepository
import java.util.UUID
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    operator fun invoke(childId: UUID) = repository.getEvents(childId)
}
