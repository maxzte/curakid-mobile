package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.EventDao
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.repository.VaccineRepository
import com.misterio.core.network.api.EventApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccineRepositoryImpl @Inject constructor(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
) : VaccineRepository {

    override fun getVaccines(childId: UUID): Flow<List<Event.Vaccine>> = eventDao
        .getEvents(childId)
        .map { entities ->
            entities.map { it.toDomain() }
                .filterIsInstance<Event.Vaccine>()
        }

    override suspend fun syncVaccines(childId: UUID): Result<Unit> {
        return safeApiCall { eventApi.getVaccineEventsFor(childId) }.map { body ->
            val remoteVaccines = body.map { it.toDomain() }
            eventDao.insertEvents(remoteVaccines.map { it.toEntity(childId) })
            Log.i(TAG, "remoteVaccines $remoteVaccines")
            Unit
        }
    }

    private companion object {
        private const val TAG = "VaccineRepository"
    }
}
