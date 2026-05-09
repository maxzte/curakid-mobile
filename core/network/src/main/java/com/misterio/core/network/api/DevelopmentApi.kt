package com.misterio.core.network.api

import com.misterio.core.network.dto.LifePeriodDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DevelopmentApi {

    @GET("api/development/life-periods")
    suspend fun getLifePeriods(): Response<List<LifePeriodDto>>

    @GET("api/development/life-periods/{code}")
    suspend fun getLifePeriod(@Path("code") code: String): Response<LifePeriodDto>

}
