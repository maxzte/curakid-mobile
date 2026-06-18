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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevelopmentRepositoryImpl @Inject constructor(
    private val dao: LifePeriodDao,
    private val developmentApi: DevelopmentApi,
) : DevelopmentRepository {

    override fun getLifePeriods(): Flow<List<LifePeriod>> {
        return dao.getLifePeriods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncLifePeriods(): Result<Unit> {
        return safeApiCall { developmentApi.getLifePeriods() }.map { body ->
            Log.d(TAG, "Syncing ${body.size} periods from network")
            dao.insertLifePeriods(body.map { it.toEntity() })
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
