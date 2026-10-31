package com.misterio.core.network.api

import com.misterio.core.network.dto.ChildResponse
import com.misterio.core.network.dto.CreateChildRequest
import com.misterio.core.network.dto.UpdateChildRequest
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

interface ChildApi {

    @GET("api/v1/children")
    suspend fun getChildren(): Response<List<ChildResponse>>

    @GET("api/v1/children/{childId}")
    suspend fun getChild(@Path("childId") childId: UUID): Response<ChildResponse>

    @POST("api/v1/children")
    suspend fun createChild(@Body child: CreateChildRequest): Response<ChildResponse>

    @PUT("api/v1/children/{childId}")
    suspend fun updateChild(@Path("childId") childId: UUID, @Body child: UpdateChildRequest): Response<ChildResponse>

    @DELETE("api/v1/children/{childId}")
    suspend fun deleteChild(@Path("childId") childId: UUID): Response<Unit>

}
