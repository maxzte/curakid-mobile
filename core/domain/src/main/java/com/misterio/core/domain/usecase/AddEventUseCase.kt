package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.repository.EventRepository
import java.util.UUID
import javax.inject.Inject

class AddEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(childId: UUID, event: Event) = repository.addEvent(childId, event)
}
