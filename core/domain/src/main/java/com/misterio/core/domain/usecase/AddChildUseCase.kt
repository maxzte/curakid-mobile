package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.ChildRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class AddChildUseCase @Inject constructor(
    private val repository: ChildRepository,
    private val scheduleVaccineEventsUseCase: ScheduleVaccineEventsUseCase,
    private val scheduleDoctorVisitsUseCase: ScheduleDoctorVisitsUseCase,
) {
    suspend operator fun invoke(
        name: String,
        birthDate: Long,
        gender: Gender,
        isPremature: Boolean = false,
        allergies: List<String> = emptyList(),
        chronicDiseases: List<String> = emptyList(),
        pastDiseases: List<String> = emptyList(),
        specialFeatures: List<String> = emptyList()
    ): Result<Unit> {
        if (name.isBlank() || birthDate <= 0L) {
            return Result.failure(Exception("All child details are required"))
        }
        return repository.addChild(
            name,
            birthDate,
            gender,
            isPremature,
            allergies,
            chronicDiseases,
            pastDiseases,
            specialFeatures
        ).onSuccess { childId ->
            repository.setSelectedChildId(childId)
            scheduleVaccineEventsUseCase(
                childId,
                childBirthDate = Instant.ofEpochMilli(birthDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            )
            scheduleDoctorVisitsUseCase(childId)
        }.map { Unit }
    }
}

