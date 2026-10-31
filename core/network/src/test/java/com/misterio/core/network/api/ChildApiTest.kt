package com.misterio.core.network.api

import com.misterio.core.network.dto.CreateChildRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

import java.util.UUID

class ChildApiTest : BaseApiTest<ChildApi>() {

    override fun createApi(retrofit: Retrofit): ChildApi = retrofit.create(ChildApi::class.java)

    @Test
    fun `getChildren sends correct request and parses list`() = runTest {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        enqueueResponse("""
            [
                {"id": "$id1", "name": "Child 1", "birthDate": "2023-01-01", "gender": "MALE", "premature": false},
                {"id": "$id2", "name": "Child 2", "birthDate": "2023-05-01", "gender": "FEMALE", "premature": true}
            ]
        """.trimIndent())

        val response = api.getChildren()
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children", request.path)
        assertEquals("GET", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(2, response.body()?.size)
        assertEquals("Child 1", response.body()?.get(0)?.name)
    }

    @Test
    fun `getChild sends correct request with path parameter`() = runTest {
        val childId = UUID.randomUUID()
        enqueueResponse("""
            {"id": "$childId", "name": "Test Child", "birthDate": "2023-01-01", "gender": "MALE", "premature": false}
        """.trimIndent())

        val response = api.getChild(childId)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children/$childId", request.path)
        assertEquals("GET", request.method)
        assertEquals("Test Child", response.body()?.name)
    }

    @Test
    fun `createChild sends correct request body`() = runTest {
        val newId = UUID.randomUUID()
        enqueueResponse("""
            {"id": "$newId", "name": "New Child", "birthDate": "2024-01-01", "gender": "FEMALE", "premature": false}
        """.trimIndent())

        val createRequest = CreateChildRequest(
            id = newId,
            name = "New Child",
            birthDate = "2024-01-01",
            gender = "FEMALE",
            isPremature = false,
            gestationalWeeks = null,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )
        val response = api.createChild(createRequest)
        val request = mockWebServer.takeRequest()

        assertEquals("/api/v1/children", request.path)
        assertEquals("POST", request.method)
        assertTrue(response.isSuccessful)
        assertEquals(newId, response.body()?.id)
    }
}
