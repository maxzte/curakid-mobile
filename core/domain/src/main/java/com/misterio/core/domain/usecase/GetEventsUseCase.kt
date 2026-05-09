package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.EventRepository
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    operator fun invoke(childId: String) = repository.getEvents(childId)
}
