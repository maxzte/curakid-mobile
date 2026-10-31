package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.LifePeriodDao
import com.misterio.core.database.entity.LifePeriodEntity
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.LifePeriodDescription
import com.misterio.core.domain.model.Milestones
import com.misterio.core.domain.repository.DevelopmentRepository
import com.misterio.core.network.api.DevelopmentApi
import com.misterio.core.network.dto.LifePeriodResponse
import com.misterio.core.network.dto.LifePeriodMilestonesResponse
import com.misterio.core.network.dto.LifePeriodDescriptionResponse
import com.misterio.curakid.core.common.AppDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevelopmentRepositoryImpl @Inject constructor(
    private val dao: LifePeriodDao,
    private val developmentApi: DevelopmentApi,
    private val appDataStore: AppDataStore,
) : DevelopmentRepository {

    override fun getLifePeriods(): Flow<List<LifePeriod>> {
        return dao.getLifePeriods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncLifePeriods(force: Boolean): Result<Unit> {
        if (!force) {
            val lastSync = appDataStore.lastLifePeriodsSync.first()
            val elapsed = System.currentTimeMillis() - lastSync
            val currentEntities = dao.getLifePeriods().first()
            val hasEmptyQuestions = currentEntities.isNotEmpty() && currentEntities.any { it.questions.isEmpty() }
            
            if (lastSync > 0 && elapsed < SYNC_INTERVAL_MS && currentEntities.isNotEmpty() && !hasEmptyQuestions) {
                Log.d(TAG, "syncLifePeriods: Skipping, last sync was ${elapsed / 1000}s ago and cache is populated")
                return Result.success(Unit)
            }
        }

        return safeApiCall { developmentApi.getLifePeriods() }.map { body ->
            Log.d(TAG, "Syncing ${body.size} periods from network")
            val entities = body.map { it.toEntity() }
            dao.syncWithServer(entities)
            appDataStore.setLastLifePeriodsSync(System.currentTimeMillis())
            Unit
        }
    }

    private fun LifePeriodResponse.toEntity() = LifePeriodEntity(
        id = id,
        code = code,
        label = label,
        about = description.toDomain(),
        // Better to parse here so Entity stores the domain model (if TypeConverter is set up)
        minMonths = minMonths,
        maxMonths = maxMonths,
        questions = questions.map { it.toDomain() }
    )

    private companion object {
        const val TAG = "DevelopmentRepository"
        const val SYNC_INTERVAL_MS = 60 * 60 * 1000L // 1 hour TTL
    }

}

private fun LifePeriodDescriptionResponse.toDomain() = LifePeriodDescription(
    title = title,
    subtitle = subtitle,
    milestones = milestones.toDomain(),
    tips = tips
)

private fun LifePeriodMilestonesResponse.toDomain() = Milestones(
    movement = movement,
    speech = speech,
    cognition = cognition,
)
