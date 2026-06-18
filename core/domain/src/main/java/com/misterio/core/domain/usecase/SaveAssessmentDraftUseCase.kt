package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.repository.AssessmentRepository
import java.util.UUID
import javax.inject.Inject

class SaveAssessmentDraftUseCase @Inject constructor(
    private val repository: AssessmentRepository,
) {
    suspend operator fun invoke(
        periodId: UUID,
        childId: UUID,
        skillAssessmentId: UUID?,
        answer: Answer,
    ): Result<ChildAssessment> {
        return if (skillAssessmentId == null) {
            repository.saveAnswerForChild(periodId, childId, answer)
        } else {
            repository.updateAnswerForChild(childId, skillAssessmentId, answer)
        }
    }
}
