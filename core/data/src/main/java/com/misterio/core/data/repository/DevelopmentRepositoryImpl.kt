package com.misterio.core.data.repository

import com.misterio.core.data.mapper.toDomain
import com.misterio.core.database.dao.LifePeriodDao
import com.misterio.core.database.entity.LifePeriodEntity
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.repository.DevelopmentRepository
import com.misterio.core.network.api.DevelopmentApi
import com.misterio.core.network.dto.LifePeriodResponse
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

    override fun getLifePeriod(code: String): Flow<LifePeriod?> {
        return dao.getLifePeriodByCode(code).map { it?.toDomain() }
    }

    override suspend fun syncLifePeriods(): Result<Unit> {
        return try {
            val response = developmentApi.getLifePeriods()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                android.util.Log.d(TAG, "Syncing ${body.size} periods from network")
                dao.insertLifePeriods(body.map { it.toEntity() })
                Result.success(Unit)
            } else {
                android.util.Log.e(TAG, "Sync failed with code: ${response.code()}")
                Result.failure(Exception("Sync life periods failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Sync error", e)
            Result.failure(e)
        }
    }

    private fun LifePeriodResponse.toEntity() = LifePeriodEntity(
        id = id,
        code = code,
        label = label,
        about = description,
        minMonths = minMonths,
        maxMonths = maxMonths,
        questions = questions.map { it.toDomain() }
    )

    private fun LifePeriodEntity.toDomain() = LifePeriod(
        id = id,
        code = code,
        label = label,
        about = about,
        minMonths = minMonths,
        maxMonths = maxMonths,
        questions = questions
    )

    private companion object {
        const val TAG = "DevelopmentRepository"
    }

}