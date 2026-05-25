package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class AiApiTest : BaseApiTest<AiApi>() {

    override fun createApi(retrofit: Retrofit): AiApi = retrofit.create(AiApi::class.java)

    @Test
    fun `getChildReports sends correct request`() = runTest {
        enqueueResponse("""
            [
                {"id": "r1", "reportText": "Normal development"},
                {"id": "r2", "reportText": "Growing fast"}
            ]
        """.trimIndent())

        val response = api.getChildReports("c123")
        val request = mockWebServer.takeRequest()

        assertEquals("/api/children/c123/reports", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(2, response.body()?.size)
    }

    @Test
    fun `getReportStatus sends correct request`() = runTest {
        enqueueResponse("true")

        val response = api.getReportStatus("r123")
        val request = mockWebServer.takeRequest()

        assertEquals("/reports/r123/status", request.path)
        assertEquals("GET", request.method)
        assertEquals(true, response.body())
    }
}
