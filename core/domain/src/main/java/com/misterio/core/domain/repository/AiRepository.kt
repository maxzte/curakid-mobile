package com.misterio.core.domain.repository

import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.model.MeasurementAiReport
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface AiRepository {

    fun getReportBy(id: UUID): Flow<MeasurementAiReport>

    fun getReports(childId: UUID): Flow<List<LifePeriodAiReport>>

    fun pollReportStatus(reportId: UUID): Flow<Result<Boolean>>

    suspend fun syncReports(childId: UUID): Result<Unit>

}
