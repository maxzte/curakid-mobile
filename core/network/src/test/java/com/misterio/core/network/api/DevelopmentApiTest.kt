package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class DevelopmentApiTest : BaseApiTest<DevelopmentApi>() {

    override fun createApi(retrofit: Retrofit): DevelopmentApi = retrofit.create(DevelopmentApi::class.java)

    @Test
    fun `getLifePeriods sends correct request`() = runTest {
        enqueueResponse("""
            [
                {"id": "p1", "code": "MONTH_1", "startAgeDays": 0, "endAgeDays": 30}
            ]
        """.trimIndent())

        val response = api.getLifePeriods()
        val request = mockWebServer.takeRequest()

        assertEquals("/api/development/life-periods", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals("MONTH_1", response.body()?.get(0)?.code)
    }
}
