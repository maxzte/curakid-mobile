package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Event
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface VaccineRepository {

    fun getVaccines(childId: UUID): Flow<List<Event.Vaccine>>

    suspend fun syncVaccines(childId: UUID): Result<Unit>

}
