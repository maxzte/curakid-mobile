package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Vaccine
import kotlinx.coroutines.flow.Flow

interface VaccineRepository {

    fun getVaccines(childId: String): Flow<List<Vaccine>>

    suspend fun syncVaccines(childId: String): Result<Unit>

}
