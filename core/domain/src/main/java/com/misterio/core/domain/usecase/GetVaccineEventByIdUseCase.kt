package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class GetVaccineEventByIdUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {
    operator fun invoke(vaccineEventId: UUID): Flow<Event.Vaccine> {
        return eventRepository.getEventById(vaccineEventId).map { 
            it as? Event.Vaccine ?: throw NoSuchElementException("Vaccine not found")
        }
    }
}
