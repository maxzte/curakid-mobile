package com.misterio.core.data.mapper

import com.misterio.core.network.dto.OptionResponse
import com.misterio.core.network.dto.QuestionResponse
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionMapperTest {

    @Test
    fun `QuestionResponse toDomain maps question and options correctly`() {
        val qId = UUID.randomUUID()
        val oId = UUID.randomUUID()
        val optionDto = OptionResponse(
            id = oId,
            text = "Yes",
            value = 1.0,
            displayOrder = 1
        )
        val questionDto = QuestionResponse(
            id = qId,
            text = "Can the child lift head?",
            domain = "MOTOR",
            weight = 1.5,
            options = listOf(optionDto)
        )

        val domainQuestion = questionDto.toDomain()

        assertEquals(qId, domainQuestion.id)
        assertEquals("Can the child lift head?", domainQuestion.text)
        assertEquals("MOTOR", domainQuestion.domain)
        assertEquals(1.5, domainQuestion.weight, 0.001)
        assertEquals(1, domainQuestion.options.size)

        val domainOption = domainQuestion.options.first()
        assertEquals(oId, domainOption.id)
        assertEquals("Yes", domainOption.text)
        assertEquals(1.0, domainOption.value, 0.001)
        assertEquals(1, domainOption.displayOrder)
    }
}
