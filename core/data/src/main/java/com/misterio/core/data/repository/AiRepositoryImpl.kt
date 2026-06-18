package com.misterio.core.data.repository

import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.ChildReportDao
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.domain.repository.AiRepository
import com.misterio.core.network.api.AiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val aiApi: AiApi,
    private val childReportDao: ChildReportDao
) : AiRepository {

    override fun getReportBy(id: UUID): Flow<MeasurementAiReport> {
        return childReportDao.getReportById(id).map { entity ->
            MeasurementAiReport(
                text = entity?.reportText ?: "",
                recommendations = entity?.recommendations ?: "",
            )
        }
    }

    override fun getReports(childId: UUID): Flow<List<LifePeriodAiReport>> {
        return childReportDao.getReports(childId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun pollReportStatus(reportId: UUID): Flow<Result<Boolean>> = flow {
        var attempts = 0
        val maxAttempts = 20 // 20 * 3s = 1 minute timeout

        while (attempts < maxAttempts) {
            val result = safeApiCall { aiApi.getReportStatus(reportId) }
            emit(result)

            if (result.isSuccess && result.getOrThrow()) {
                break
            }
            if (result.isFailure) {
                break
            }

            attempts++
            delay(3000)
        }

        if (attempts >= maxAttempts) {
            emit(Result.failure(Exception("Перевищено час очікування звіту $reportId")))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncReports(childId: UUID): Result<Unit> {
        return safeApiCall { aiApi.getChildReports(childId) }.map { body ->
            childReportDao.insertReports(body.map { it.toEntity() })
            Unit
        }
    }

    private companion object {
        private const val TAG = "AiRepository"
    }
}
