package com.misterio.core.network.api

import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.network.dto.AddMeasurementRequest
import com.misterio.core.network.dto.MeasurementResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface MeasurementApi {

    @GET("api/children/{childId}/measurements")
    suspend fun getMeasurements(@Path("childId") childId: UUID): Response<List<MeasurementResponse>>

    @POST("api/children/{childId}/measurements")
    suspend fun addMeasurement(
        @Path("childId") childId: UUID,
        @Body measurement: AddMeasurementRequest
    ): Response<MeasurementResponse>

    @GET("api/measurements/norms/{type}/{gender}")
    suspend fun getMeasurementNorms(
        @Path("type") type: String,
        @Path("gender") gender: String,
    ): Response<List<ChartNormPoint>>

}
