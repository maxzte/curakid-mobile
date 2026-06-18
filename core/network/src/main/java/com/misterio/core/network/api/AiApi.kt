package com.misterio.core.network.api

import com.misterio.core.network.dto.ReportResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.UUID

interface AiApi {

    @GET("api/children/{childId}/reports")
    suspend fun getChildReports(
        @Path("childId") childId: UUID
    ): Response<List<ReportResponse>>

    @GET("api/reports/{reportId}/status")
    suspend fun getReportStatus(@Path ("reportId") reportId: UUID): Response<Boolean>

}
