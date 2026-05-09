package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.repository.VaccineRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ScheduleVaccineEventsUseCase @Inject constructor(
    private val childRepository: ChildRepository,
    private val vaccineRepository: VaccineRepository,
    private val addEventUseCase: AddEventUseCase,
    private val syncVaccinesUseCase: SyncVaccinesUseCase
) {
    suspend operator fun invoke(childId: String): Result<Unit> {
        return try {
            val child = childRepository.getChild(childId) ?: return Result.failure(Exception("Child not found"))
            
            // Sync vaccines first to make sure we have the latest schedule
            syncVaccinesUseCase(childId)
            
            val vaccines = vaccineRepository.getVaccines(childId).first()
            val today = LocalDateTime.now()
            
            vaccines.forEach { vaccine ->
                val ageMonths = vaccine.scheduledAgeMonths ?: 0
                val eventDate = child.birthDate.plusMonths(ageMonths.toLong()).atStartOfDay()
                
                // "бери від сьогодні до скільки треба" - schedule only future events or today's
                if (eventDate.isAfter(today) || eventDate.toLocalDate().isEqual(today.toLocalDate())) {
                    val event = Event(
                        id = UUID.randomUUID().toString(),
                        title = "Вакцинація: ${vaccine.name}",
                        type = EventType.VACCINE,
                        date = eventDate,
                        description = "Планова вакцинація від ${vaccine.disease}. Доза ${vaccine.doseNumber}/${vaccine.totalDoses}"
                    )
                    addEventUseCase(childId, event)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
