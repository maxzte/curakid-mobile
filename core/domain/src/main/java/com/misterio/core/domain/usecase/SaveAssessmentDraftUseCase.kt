package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.repository.AssessmentRepository
import javax.inject.Inject

class SaveAssessmentDraftUseCase @Inject constructor(
    private val repository: AssessmentRepository
) {
    suspend operator fun invoke(
        childId: String,
        domain: String,
        rawData: Map<String, Any>,
        existingAssessmentId: String?
    ): Result<ChildAssessment> {
        return if (existingAssessmentId == null) {
            repository.saveChildAssessment(childId, domain, rawData)
        } else {
            repository.updateChildAssessment(childId, existingAssessmentId, domain, rawData)
        }
    }
}
