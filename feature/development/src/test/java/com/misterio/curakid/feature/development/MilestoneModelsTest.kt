package com.misterio.curakid.feature.development

import com.misterio.core.domain.model.Option
import com.misterio.core.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class MilestoneModelsTest {

    @Test
    fun `MilestoneCategory fromDomain maps correctly`() {
        assertEquals(MilestoneCategory.MOTOR, MilestoneCategory.fromDomain("MOTOR_GROSS"))
        assertEquals(MilestoneCategory.MOTOR, MilestoneCategory.fromDomain("motor_fine"))
        assertEquals(MilestoneCategory.SPEECH, MilestoneCategory.fromDomain("SPEECH"))
        assertEquals(MilestoneCategory.COGNITION, MilestoneCategory.fromDomain("COGNITIVE"))
        assertEquals(MilestoneCategory.SOCIAL, MilestoneCategory.fromDomain("SOCIAL_EMOTIONAL"))
        assertEquals(MilestoneCategory.SENSORY, MilestoneCategory.fromDomain("SENSORY"))
        assertEquals(MilestoneCategory.SELF_CARE, MilestoneCategory.fromDomain("SELF_CARE"))
        assertEquals(MilestoneCategory.MOTOR, MilestoneCategory.fromDomain("UNKNOWN"))
    }

    @Test
    fun `toSummary calculates correct counts and progress`() {
        val q1 = createQuestion("q1")
        val q2 = createQuestion("q2")
        val q3 = createQuestion("q3")
        val q4 = createQuestion("q4")
        val questions = listOf(q1, q2, q3, q4)

        val answers = listOf(
            AnswerUI(assessmentId = null, questionId = q1.id, option = Option(UUID.randomUUID(), "Yes", 100.0, 1)),
            AnswerUI(assessmentId = null, questionId = q2.id, option = Option(UUID.randomUUID(), "Partial", 50.0, 2)),
            AnswerUI(assessmentId = null, questionId = q3.id, option = Option(UUID.randomUUID(), "No", 0.0, 3))
        )

        val summary = questions.toSummary(answers)

        assertEquals(4, summary.totalQuestions)
        assertEquals(1, summary.answeredYes)
        assertEquals(1, summary.answeredPartial)
        assertEquals(1, summary.answeredNo)
        assertEquals(3, summary.completed)
        assertEquals(0.75f, summary.progressFraction, 0.001f)
    }

    private fun createQuestion(text: String) = Question(
        id = UUID.randomUUID(),
        text = text,
        domain = "MOTOR_GROSS",
        weight = 1.0,
        options = listOf(
            Option(UUID.randomUUID(), "Yes", 100.0, 1),
            Option(UUID.randomUUID(), "Partial", 50.0, 2),
            Option(UUID.randomUUID(), "No", 0.0, 3)
        )
    )
}
