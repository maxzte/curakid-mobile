package com.misterio.core.network.api

import com.misterio.core.network.dto.ReportResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface AiApi {

    @GET("api/v1/children/{childId}/reports")
    suspend fun getChildReports(
        @Path("childId") childId: UUID
    ): Response<List<ReportResponse>>

    @POST("api/v1/children/{childId}/reports/generate")
    suspend fun generateReport(
        @Path("childId") childId: UUID
    ): Response<ReportResponse>

    @GET("api/v1/reports/{reportId}")
    suspend fun getReport(
        @Path("reportId") reportId: UUID
    ): Response<ReportResponse>

    @GET("api/v1/reports/{reportId}/status")
    suspend fun getReportStatus(@Path ("reportId") reportId: UUID): Response<Boolean>

    @POST("api/v1/reports/{summaryId}/regenerate")
    suspend fun regenerateReport(
        @Path("summaryId") summaryId: UUID
    ): Response<ReportResponse>

}
