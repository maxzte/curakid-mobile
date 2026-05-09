package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.repository.AssessmentRepository
import javax.inject.Inject

class GetChildAssessmentsUseCase @Inject constructor(
    private val repository: AssessmentRepository
) {
    suspend operator fun invoke(childId: String, domain: String? = null): Result<List<ChildAssessment>> {
        return repository.getChildAssessments(childId, domain)
    }
}
