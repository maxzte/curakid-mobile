package com.misterio.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class LegalDocumentApiTest : BaseApiTest<LegalDocumentApi>() {

    override fun createApi(retrofit: Retrofit): LegalDocumentApi = retrofit.create(LegalDocumentApi::class.java)

    @Test
    fun `getAllDocuments sends correct request and parses response`() = runTest {
        enqueueResponse("""
            [
                {
                    "type": "TERMS_OF_SERVICE",
                    "version": "1.0",
                    "url": "https://example.com/terms.html",
                    "publishedAt": "2025-01-01T00:00:00Z"
                }
            ]
        """.trimIndent())

        val response = api.getAllDocuments()
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/legal", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
        assertEquals("TERMS_OF_SERVICE", response.body()?.first()?.type)
    }

    @Test
    fun `getByType sends correct path and parses document`() = runTest {
        enqueueResponse("""
            {
                "type": "PRIVACY_POLICY",
                "version": "2.0",
                "url": "https://example.com/privacy.html",
                "publishedAt": "2025-02-01T00:00:00Z"
            }
        """.trimIndent())

        val response = api.getByType("PRIVACY_POLICY")
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/legal/PRIVACY_POLICY", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals("PRIVACY_POLICY", response.body()?.type)
        assertEquals("2.0", response.body()?.version)
    }

    @Test
    fun `downloadContent fetches body content successfully`() = runTest {
        enqueueResponse("<html>Privacy Content</html>")

        val responseBody = api.downloadContent(mockWebServer.url("/legal/privacy.html").toString())
        val request = mockWebServer.takeRequest()

        assertEquals("/legal/privacy.html", request.path)
        assertEquals("GET", request.method)
        assertEquals("<html>Privacy Content</html>", responseBody.string())
    }
}
