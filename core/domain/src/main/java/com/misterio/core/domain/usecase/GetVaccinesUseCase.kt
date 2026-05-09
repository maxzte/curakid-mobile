package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Vaccine
import com.misterio.core.domain.repository.VaccineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVaccinesUseCase @Inject constructor(
    private val vaccineRepository: VaccineRepository
) {
    operator fun invoke(childId: String): Flow<List<Vaccine>> {
        return vaccineRepository.getVaccines(childId)
    }
}
