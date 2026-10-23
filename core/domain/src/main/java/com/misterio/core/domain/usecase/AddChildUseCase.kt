package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.ChildRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class AddChildUseCase @Inject constructor(
    private val repository: ChildRepository,
) {
    suspend operator fun invoke(
        name: String,
        birthDate: Long,
        gender: Gender,
        isPremature: Boolean = false,
        gestationalWeeks: Int? = null,
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
            gestationalWeeks,
            allergies,
            chronicDiseases,
            pastDiseases,
            specialFeatures
        ).onSuccess { childId ->
            repository.setSelectedChildId(childId)
        }.map { Unit }
    }
}

