package com.misterio.core.data.mapper

import com.misterio.core.database.entity.LifePeriodEntity
import com.misterio.core.domain.model.LifePeriodDescription
import com.misterio.core.domain.model.Milestones
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class LifePeriodMapperTest {

    @Test
    fun `LifePeriodEntity toDomain correctly maps fields`() {
        val id = UUID.randomUUID()
        val desc = LifePeriodDescription(
            title = "Title",
            subtitle = "Subtitle",
            milestones = Milestones("Moves", "Speaks", "Thinks"),
            tips = listOf("Tip 1")
        )
        val entity = LifePeriodEntity(
            id = id,
            code = "MONTH_0_1",
            label = "0-1 Month",
            about = desc,
            minMonths = 0,
            maxMonths = 1,
            questions = emptyList()
        )

        val domain = entity.toDomain()

        assertEquals(id, domain.id)
        assertEquals("MONTH_0_1", domain.code)
        assertEquals("0-1 Month", domain.label)
        assertEquals(desc, domain.about)
        assertEquals(0, domain.minMonths)
        assertEquals(1, domain.maxMonths)
    }
}
