package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.database.dao.VaccineDao
import com.misterio.core.database.entity.VaccineEventEntity
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.VaccineEvent
import com.misterio.core.domain.model.VaccineType
import com.misterio.core.domain.repository.VaccineRepository
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.dto.VaccineEventResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccineRepositoryImpl @Inject constructor(
    private val eventApi: EventApi,
    private val vaccineDao: VaccineDao,
) : VaccineRepository {

    override fun getVaccines(childId: String): Flow<List<VaccineEvent>> = vaccineDao
        .getVaccinesByChild(childId)
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncVaccines(childId: String): Result<Unit> {
        return try {
            val response = eventApi.getVaccineEventsFor(childId)
            if (response.isSuccessful && response.body() != null) {
                vaccineDao.insertVaccines(response.body()!!.map { it.toEntity() })
                Log.i(TAG, "remoteVaccines ${response.body()}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch vaccines: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun VaccineEventEntity.toDomain() = VaccineEvent(
        id = id,
        name = name,
        date = date,
        disease = disease,
        doseNumber = doseNumber,
        totalDoses = totalDoses,
        type = VaccineType.valueOf(type),
        scheduledAgeMonths = scheduledAgeMonths,
        scheduledAgeDays = scheduledAgeDays,
        notes = notes,
        status = EventStatus.valueOf(status),
    )

    private fun VaccineEventResponse.toEntity() = VaccineEventEntity(
        id = id,
        childId = childId,
        name = name,
        date = LocalDateTime.parse(date),
        disease = disease,
        doseNumber = doseNumber,
        totalDoses = totalDoses,
        type = vaccineType.name,
        scheduledAgeMonths = scheduledAgeMonths,
        scheduledAgeDays = scheduledAgeDays,
        notes = notes,
        status = status.name,
    )

    private companion object {
        private const val TAG = "VaccineRepository"
    }
}
