package com.misterio.core.network.api

import com.misterio.core.network.dto.EventRequest
import com.misterio.core.network.dto.EventResponse
import com.misterio.core.network.dto.UpdateEventRequest
import com.misterio.core.network.dto.VaccineEventResponse
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

private const val prefix = "/api/children/{childId}/events"

interface EventApi {

    @GET(prefix)
    suspend fun getEvents(@Path("childId") childId: UUID): Response<List<EventResponse>>

    @GET("$prefix/vaccines")
    suspend fun getVaccineEventsFor(@Path("childId") childId: String): Response<List<VaccineEventResponse>>

    @GET("$prefix/{eventId}")
    suspend fun getEvent(@Path("eventId") eventId: UUID): Response<EventResponse>

    @POST(prefix)
    suspend fun createEvent(@Body event: EventRequest): Response<EventResponse>

    @POST("$prefix/batch")
    suspend fun createEvents(@Body events: List<EventRequest>): Response<List<EventResponse>>

    @PUT("$prefix/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: UUID,
        @Body event: UpdateEventRequest
    ): Response<EventResponse>

    @DELETE("$prefix/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: UUID): Response<Unit>

}
