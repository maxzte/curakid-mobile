package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

import java.util.UUID

class AiApiTest : BaseApiTest<AiApi>() {

    override fun createApi(retrofit: Retrofit): AiApi = retrofit.create(AiApi::class.java)

    @Test
    fun `getChildReports sends correct request`() = runTest {
        val childId = UUID.randomUUID()
        enqueueResponse("""
            [
                {"id": "${UUID.randomUUID()}", "reportText": "Normal development"},
                {"id": "${UUID.randomUUID()}", "reportText": "Growing fast"}
            ]
        """.trimIndent())

        val response = api.getChildReports(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children/$childId/reports", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(2, response.body()?.size)
    }

    @Test
    fun `getReportStatus sends correct request`() = runTest {
        val reportId = UUID.randomUUID()
        enqueueResponse("true")

        val response = api.getReportStatus(reportId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/reports/$reportId/status", request.path)
        assertEquals("GET", request.method)
        assertEquals(true, response.body())
    }
}
