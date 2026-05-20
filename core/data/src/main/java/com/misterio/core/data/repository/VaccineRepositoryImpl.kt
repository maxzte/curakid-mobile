package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.database.dao.EventDao
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.repository.VaccineRepository
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.dto.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccineRepositoryImpl @Inject constructor(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
) : VaccineRepository {

    override fun getVaccines(childId: String): Flow<List<Event.Vaccine>> = eventDao
        .getEvents(childId)
        .map { entities ->
            entities.map { it.toDomain() }
                .filterIsInstance<Event.Vaccine>()
        }

    override suspend fun syncVaccines(childId: String): Result<Unit> {
        return try {
            val response = eventApi.getVaccineEventsFor(childId)
            if (response.isSuccessful && response.body() != null) {
                val remoteVaccines = response.body()!!.map { it.toDomain() }
                eventDao.insertEvents(remoteVaccines.map { it.toEntity(childId) })
                Log.i(TAG, "remoteVaccines $remoteVaccines")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch vaccines: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    private companion object {
        private const val TAG = "VaccineRepository"
    }
}
