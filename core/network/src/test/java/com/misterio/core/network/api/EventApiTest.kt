package com.misterio.core.network.api

import com.misterio.core.domain.model.EventType
import com.misterio.core.network.dto.EventRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import java.util.UUID

class EventApiTest : BaseApiTest<EventApi>() {

    override fun createApi(retrofit: Retrofit): EventApi = retrofit.create(EventApi::class.java)

    @Test
    fun `getEvents sends correct request`() = runTest {
        val childId = UUID.randomUUID()
        enqueueResponse("""
            [
                {"id": "${UUID.randomUUID()}", "title": "Checkup", "type": "MEDICAL"}
            ]
        """.trimIndent())

        val response = api.getEvents(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/children/$childId/events", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
    }

    @Test
    fun `createEvent sends correct body`() = runTest {
        val childId = UUID.randomUUID()
        val eventRequest = EventRequest(
            title = "Vaccine",
            type = EventType.VACCINE,
            date = "2024-01-01T10:00:00",
            childId = childId.toString()
        )
        
        enqueueResponse("""
            {"id": "${UUID.randomUUID()}", "title": "Vaccine", "childId": "$childId", "type": "VACCINE", "date": "2024-01-01T10:00:00", "status": "SCHEDULED"}
        """.trimIndent())

        val response = api.createEvent(childId, eventRequest)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/children/$childId/events", request.path)
        assertEquals("POST", request.method)
        assertEquals("Vaccine", response.body()?.title)
    }
}
