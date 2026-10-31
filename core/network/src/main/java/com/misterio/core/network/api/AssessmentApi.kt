package com.misterio.core.network.api

import com.misterio.core.network.dto.AnswerRequest
import com.misterio.core.network.dto.AssessmentResponse
import com.misterio.core.network.dto.CompletePeriodAssessmentRequest
import com.misterio.core.network.dto.CompletePeriodAssessmentResponse
import com.misterio.core.network.dto.PeriodReadinessResponse
import com.misterio.core.network.dto.SaveAnswersRequest
import com.misterio.core.network.dto.SaveAnswersResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface AssessmentApi {

    @GET("api/v1/children/{childId}/assessments")
    suspend fun getAssessments(
        @Path("childId") childId: UUID,
    ): Response<List<AssessmentResponse>>

    @GET("api/v1/children/{childId}/assessments/readiness")
    suspend fun getReadiness(
        @Path("childId") childId: UUID,
        @Query("periodCode") periodCode: String
    ): Response<PeriodReadinessResponse>

    @POST("api/v1/children/{childId}/assessments/{periodId}")
    suspend fun saveAssessmentAnswers(
        @Path("childId") childId: UUID,
        @Path("periodId") periodId: UUID,
        @Body request: SaveAnswersRequest
    ): Response<SaveAnswersResponse>

    @PATCH("api/v1/children/{childId}/assessments/{assessmentId}")
    suspend fun updateAssessmentAnswers(
        @Path("childId") childId: UUID,
        @Path("assessmentId") assessmentId: UUID,
        @Body request: AnswerRequest
    ): Response<AssessmentResponse>

    @POST("api/v1/children/{childId}/assessments/complete")
    suspend fun completeAssessment(
        @Path("childId") childId: UUID,
        @Body request: CompletePeriodAssessmentRequest
    ): Response<CompletePeriodAssessmentResponse>

}
