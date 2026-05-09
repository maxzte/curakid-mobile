package com.misterio.core.network.api

import com.misterio.core.network.dto.AssessmentResponse
import com.misterio.core.network.dto.CompletePeriodAssessmentRequest
import com.misterio.core.network.dto.CompletePeriodAssessmentResponse
import com.misterio.core.network.dto.SaveAssessmentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AssessmentApi {

    @GET("api/children/{childId}/assessments")
    suspend fun getAssessments(
        @Path("childId") childId: String,
        @Query("domain") domain: String? = null
    ): Response<List<AssessmentResponse>>

    @POST("api/children/{childId}/assessments")
    suspend fun saveAssessment(
        @Path("childId") childId: String,
        @Body request: SaveAssessmentRequest
    ): Response<AssessmentResponse>

    @PATCH("api/children/{childId}/assessments/{assessmentId}")
    suspend fun updateAssessment(
        @Path("childId") childId: String,
        @Path("assessmentId") assessmentId: String,
        @Body request: SaveAssessmentRequest
    ): Response<AssessmentResponse>

    @POST("api/children/{childId}/assessments/complete")
    suspend fun completeAssessment(
        @Path("childId") childId: String,
        @Body request: CompletePeriodAssessmentRequest
    ): Response<CompletePeriodAssessmentResponse>

}
