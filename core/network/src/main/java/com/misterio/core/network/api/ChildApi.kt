package com.misterio.core.network.api

import com.misterio.core.network.dto.ChildDto
import retrofit2.Response
import retrofit2.http.*

interface ChildApi {
    @GET("api/children")
    suspend fun getChildren(): Response<List<ChildDto>>

    @GET("api/children/{childId}")
    suspend fun getChild(@Path("childId") childId: String): Response<ChildDto>

    @POST("api/children")
    suspend fun createChild(@Body child: ChildDto): Response<ChildDto>

    @PUT("api/children/{childId}")
    suspend fun updateChild(@Path("childId") childId: String, @Body child: ChildDto): Response<ChildDto>

    @DELETE("api/children/{childId}")
    suspend fun deleteChild(@Path("childId") childId: String): Response<Unit>
}
