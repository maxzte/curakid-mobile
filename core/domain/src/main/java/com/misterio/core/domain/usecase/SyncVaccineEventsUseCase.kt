package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.VaccineRepository
import javax.inject.Inject

class SyncVaccineEventsUseCase @Inject constructor(
    private val vaccineRepository: VaccineRepository
) {
    suspend operator fun invoke(childId: String): Result<Unit> {
        return vaccineRepository.syncVaccines(childId)
    }
}
