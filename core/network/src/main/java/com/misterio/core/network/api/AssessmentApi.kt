package com.misterio.core.network.api

import com.misterio.core.network.dto.AnswerRequest
import com.misterio.core.network.dto.AssessmentResponse
import com.misterio.core.network.dto.CompletePeriodAssessmentRequest
import com.misterio.core.network.dto.CompletePeriodAssessmentResponse
import com.misterio.core.network.dto.SaveAnswersRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AssessmentApi {

    @GET("api/children/{childId}/assessments")
    suspend fun getAssessments(
        @Path("childId") childId: String,
    ): Response<List<AssessmentResponse>>

    @POST("api/children/{childId}/assessments/{periodId}")
    suspend fun saveAssessmentAnswers(
        @Path("childId") childId: String,
        @Path("periodId") periodId: String,
        @Body request: SaveAnswersRequest
    ): Response<List<AssessmentResponse>>

    @PATCH("api/children/{childId}/assessments/{assessmentId}")
    suspend fun updateAssessmentAnswers(
        @Path("childId") childId: String,
        @Path("assessmentId") assessmentId: String,
        @Body request: AnswerRequest
    ): Response<AssessmentResponse>

    @POST("api/children/{childId}/assessments/complete")
    suspend fun completeAssessment(
        @Path("childId") childId: String,
        @Body request: CompletePeriodAssessmentRequest
    ): Response<CompletePeriodAssessmentResponse>

}
