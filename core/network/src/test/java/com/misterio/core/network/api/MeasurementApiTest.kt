package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

import java.util.UUID

class MeasurementApiTest : BaseApiTest<MeasurementApi>() {

    override fun createApi(retrofit: Retrofit): MeasurementApi = retrofit.create(MeasurementApi::class.java)

    @Test
    fun `getMeasurements sends correct request`() = runTest {
        val childId = UUID.randomUUID()
        enqueueResponse("""
            {
                "content": [
                    {"id": "${UUID.randomUUID()}", "childId": "$childId", "weightKg": 5.0, "heightCm": 60.0, "headCircCm": 40.0, "measurementDate": "2024-01-01T10:00:00"}
                ],
                "pageNumber": 0,
                "pageSize": 100,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
            }
        """.trimIndent())

        val response = api.getMeasurements(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children/$childId/measurements?page=0&size=100", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.content?.size)
    }
}
