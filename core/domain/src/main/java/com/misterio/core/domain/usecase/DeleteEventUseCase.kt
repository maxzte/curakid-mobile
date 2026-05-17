package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.EventRepository
import javax.inject.Inject

class DeleteEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Result<Unit> =
        repository.deleteEvent(eventId)
}
