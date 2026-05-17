package com.misterio.core.domain.repository

import com.misterio.core.domain.model.VaccineEvent
import kotlinx.coroutines.flow.Flow

interface VaccineRepository {

    fun getVaccines(childId: String): Flow<List<VaccineEvent>>

    suspend fun syncVaccines(childId: String): Result<Unit>

}
