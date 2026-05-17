package com.misterio.core.network.api

import com.misterio.core.network.dto.AddMeasurementRequest
import com.misterio.core.network.dto.MeasurementResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MeasurementApi {

    @GET("api/children/{childId}/measurements")
    suspend fun getMeasurements(@Path("childId") childId: String): Response<List<MeasurementResponse>>

    @POST("api/children/{childId}/measurements")
    suspend fun addMeasurement(
        @Path("childId") childId: String,
        @Body measurement: AddMeasurementRequest
    ): Response<MeasurementResponse>

}
