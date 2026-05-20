package com.misterio.core.network.api

import com.misterio.core.network.dto.ChildResponse
import com.misterio.core.network.dto.CreateChildRequest
import retrofit2.Response
import retrofit2.http.*

interface ChildApi {
    @GET("api/children")
    suspend fun getChildren(): Response<List<ChildResponse>>

    @GET("api/children/{childId}")
    suspend fun getChild(@Path("childId") childId: String): Response<ChildResponse>

    @POST("api/children")
    suspend fun createChild(@Body child: CreateChildRequest): Response<ChildResponse>

    @PUT("api/children/{childId}")
    suspend fun updateChild(@Path("childId") childId: String, @Body child: ChildResponse): Response<ChildResponse>

    @DELETE("api/children/{childId}")
    suspend fun deleteChild(@Path("childId") childId: String): Response<Unit>
}
