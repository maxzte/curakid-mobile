package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class MeasurementApiTest : BaseApiTest<MeasurementApi>() {

    override fun createApi(retrofit: Retrofit): MeasurementApi = retrofit.create(MeasurementApi::class.java)

    @Test
    fun `getMeasurements sends correct request`() = runTest {
        enqueueResponse("""
            [
                {"id": "m1", "childId": "c1", "weight": 5.0, "height": 60.0, "headCircumference": 40.0, "measuredAt": "2024-01-01"}
            ]
        """.trimIndent())

        val response = api.getMeasurements("c1")
        val request = mockWebServer.takeRequest()

        assertEquals("/api/children/c1/measurements", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
    }
}
