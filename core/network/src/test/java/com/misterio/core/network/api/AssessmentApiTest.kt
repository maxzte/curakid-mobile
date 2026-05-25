package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class AssessmentApiTest : BaseApiTest<AssessmentApi>() {

    override fun createApi(retrofit: Retrofit): AssessmentApi = retrofit.create(AssessmentApi::class.java)

    @Test
    fun `getAssessments sends correct request`() = runTest {
        enqueueResponse("""
            [
                {"id": "a1", "childId": "c1", "assessmentDate": "2024-01-01", "answers": []}
            ]
        """.trimIndent())

        val response = api.getAssessments("c1")
        val request = mockWebServer.takeRequest()

        assertEquals("/api/children/c1/assessments", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
    }
}
