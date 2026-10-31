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
            {
                "content": [
                    {"id": "${UUID.randomUUID()}", "childId": "$childId", "title": "Checkup", "type": "MEDICAL", "date": "2024-01-01T10:00:00", "status": "PLANNED"}
                ],
                "pageNumber": 0,
                "pageSize": 100,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
            }
        """.trimIndent())

        val response = api.getEvents(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children/$childId/events?page=0&size=100", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.content?.size)
    }

    @Test
    fun `createEvent sends correct body`() = runTest {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val eventRequest = EventRequest(
            id = id,
            childId = childId,
            title = "Vaccine",
            type = EventType.VACCINE,
            date = "2024-01-01T10:00:00",
            status = com.misterio.core.domain.model.EventStatus.PLANNED
        )
        
        enqueueResponse("""
            {"id": "$id", "title": "Vaccine", "childId": "$childId", "type": "VACCINE", "date": "2024-01-01T10:00:00", "status": "PLANNED"}
        """.trimIndent())

        val response = api.createEvent(childId, eventRequest)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children/$childId/events", request.path)
        assertEquals("POST", request.method)
        assertEquals("Vaccine", response.body()?.title)
    }
}
