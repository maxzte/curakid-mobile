package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.EventRepository
import javax.inject.Inject

class SyncEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(childId: String) = eventRepository.syncEvents(childId)
}
