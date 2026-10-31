package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

import java.util.UUID

class AssessmentApiTest : BaseApiTest<AssessmentApi>() {

    override fun createApi(retrofit: Retrofit): AssessmentApi = retrofit.create(AssessmentApi::class.java)

    @Test
    fun `getAssessments sends correct request`() = runTest {
        val childId = UUID.randomUUID()
        enqueueResponse("""
            [
                {"id": "${UUID.randomUUID()}", "childId": "$childId", "assessmentDate": "2024-01-01", "answers": []}
            ]
        """.trimIndent())

        val response = api.getAssessments(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children/$childId/assessments", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
    }
}
