package com.misterio.core.network.api

import com.misterio.core.network.dto.UpdateVaccineRequest
import com.misterio.core.network.dto.VaccineDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface VaccineApi {

    @GET("api/children/{childId}/vaccines")
    suspend fun getVaccines(@Path("childId") childId: String): Response<List<VaccineDto>>

    @PATCH("api/children/{childId}/vaccines/{vaccineId}")
    suspend fun updateVaccine(
        @Path("childId") childId: String,
        @Path("vaccineId") vaccineId: String,
        @Body request: UpdateVaccineRequest,
    ): Response<VaccineDto>

}
