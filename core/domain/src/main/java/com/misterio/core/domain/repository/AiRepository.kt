package com.misterio.core.domain.repository

import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.model.MeasurementAiReport
import kotlinx.coroutines.flow.Flow

interface AiRepository {

    fun getReportBy(id: String): Flow<MeasurementAiReport>

    fun getReports(childId: String): Flow<List<LifePeriodAiReport>>

    fun pollReportStatus(reportId: String): Flow<Result<Boolean>>

    suspend fun syncReports(childId: String): Result<Unit>

}
