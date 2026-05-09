package com.misterio.core.network.api

import com.misterio.core.network.dto.EventRequest
import com.misterio.core.network.dto.EventResponse
import com.misterio.core.network.dto.UpdateEventRequest
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

interface EventApi {

    @GET("api/events/child/{childId}")
    suspend fun getEvents(@Path("childId") childId: UUID): Response<List<EventResponse>>

    @GET("api/events/{eventId}")
    suspend fun getEvent(@Path("eventId") eventId: UUID): Response<EventResponse>

    @POST("api/events")
    suspend fun createEvent(@Body event: EventRequest): Response<EventResponse>

    @PUT("api/events/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: UUID,
        @Body event: UpdateEventRequest
    ): Response<EventResponse>

    @DELETE("api/events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: UUID): Response<Unit>
}
