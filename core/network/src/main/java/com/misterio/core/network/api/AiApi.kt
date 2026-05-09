package com.misterio.core.network.api

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface AiApi {
    @POST("api/children/{childId}/reports")
    suspend fun requestAnalysis(@Path("childId") childId: String): Response<AiReportResponse>
}

data class AiReportResponse(
    val report: String
)
