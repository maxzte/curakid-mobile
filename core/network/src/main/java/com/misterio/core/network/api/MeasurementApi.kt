package com.misterio.core.network.api

import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.network.dto.AddMeasurementRequest
import com.misterio.core.network.dto.MeasurementResponse
import com.misterio.core.network.dto.PagedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface MeasurementApi {

    @GET("api/v1/children/{childId}/measurements")
    suspend fun getMeasurements(
        @Path("childId") childId: UUID,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PagedResponse<MeasurementResponse>>

    @POST("api/v1/children/{childId}/measurements")
    suspend fun addMeasurement(
        @Path("childId") childId: UUID,
        @Body measurement: AddMeasurementRequest
    ): Response<MeasurementResponse>

    @PUT("api/v1/children/{childId}/measurements/{measurementId}")
    suspend fun updateMeasurement(
        @Path("childId") childId: UUID,
        @Path("measurementId") measurementId: UUID,
        @Body measurement: AddMeasurementRequest
    ): Response<MeasurementResponse>

    @DELETE("api/v1/children/{childId}/measurements/{measurementId}")
    suspend fun deleteMeasurement(
        @Path("childId") childId: UUID,
        @Path("measurementId") measurementId: UUID
    ): Response<Unit>

    @GET("api/v1/measurements/norms/{type}/{gender}")
    suspend fun getMeasurementNorms(
        @Path("type") type: String,
        @Path("gender") gender: String,
    ): Response<List<ChartNormPoint>>

}
