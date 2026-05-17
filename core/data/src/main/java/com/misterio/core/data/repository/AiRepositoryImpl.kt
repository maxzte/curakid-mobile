package com.misterio.core.data.repository

import com.misterio.core.database.dao.ChildReportDao
import com.misterio.core.database.entity.ChildReportEntity
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.domain.repository.AiRepository
import com.misterio.core.network.api.AiApi
import com.misterio.core.network.dto.ReportResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val aiApi: AiApi,
    private val childReportDao: ChildReportDao
) : AiRepository {

    override fun getReportBy(id: String): Flow<MeasurementAiReport> {
        return childReportDao.getReportById(id).map { entity ->
            MeasurementAiReport(
                text = entity?.reportText ?: "",
                recommendations = entity?.recommendations ?: "",
            )
        }
    }

    override fun getReports(childId: String): Flow<List<LifePeriodAiReport>> {
        return childReportDao.getReports(childId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun pollReportStatus(reportId: String): Flow<Result<Boolean>> = flow {
        var attempts = 0
        val maxAttempts = 20 // 20 * 3s = 1 minute timeout

        while (attempts < maxAttempts) {
            try {
                val res = aiApi.getReportStatus(reportId)
                val body = res.body()

                emit(Result.success(body ?: false))

                if (body != null && body) {
                    break
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
                break
            }

            attempts++
            delay(3000)
        }
        
        if (attempts >= maxAttempts) {
            emit(Result.failure(Exception("Polling timeout for report $reportId")))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncReports(childId: String): Result<Unit> {
        return try {
            val res = aiApi.getChildReports(childId)
            val body = res.body()
            if (res.isSuccessful && body != null) {
                childReportDao.insertReports(body.map { it.toEntity() })
                Result.success(Unit)
            } else {
                throw Exception()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        private const val TAG = "AiRepository"
    }
}

private fun ReportResponse.toEntity() = ChildReportEntity(
    id = id,
    childId = childId,
    lifePeriodId = lifePeriodId,
    triggerMeasurementId = triggerMeasurementId,
    reportText = reportText,
    recommendations = recommendations,
    isFallback = isFallback,
    urgencyScore = urgencyScore,
    createdAt = LocalDateTime.parse(createdAt)
)

private fun ChildReportEntity.toDomain() = LifePeriodAiReport(
    id = id,
    childId = childId,
    lifePeriodId = lifePeriodId,
    triggerMeasurementId = triggerMeasurementId,
    reportText = reportText,
    recommendations = recommendations,
    isFallback = isFallback,
    urgencyScore = urgencyScore
)
