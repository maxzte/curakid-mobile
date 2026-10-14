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
            [
                {"id": "${UUID.randomUUID()}", "childId": "$childId", "weight": 5.0, "height": 60.0, "headCircumference": 40.0, "measuredAt": "2024-01-01"}
            ]
        """.trimIndent())

        val response = api.getMeasurements(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/children/$childId/measurements", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
    }
}
