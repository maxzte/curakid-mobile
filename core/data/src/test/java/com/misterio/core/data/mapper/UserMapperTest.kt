package com.misterio.core.data.mapper

import com.misterio.core.network.dto.UserResponse
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserMapperTest {

    @Test
    fun `UserResponse toDomain maps all fields correctly`() {
        val id = UUID.randomUUID()
        val dto = UserResponse(
            id = id,
            email = "user@example.com",
            name = "Jane Doe",
            role = "PARENT",
            onboardingCompleted = true,
            active = true
        )

        val domain = dto.toDomain()

        assertEquals(id, domain.id)
        assertEquals("user@example.com", domain.email)
        assertEquals("Jane Doe", domain.name)
        assertEquals("PARENT", domain.role)
        assertTrue(domain.onboardingCompleted)
        assertTrue(domain.active)
    }
}
