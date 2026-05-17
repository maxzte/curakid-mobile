package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.repository.AssessmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChildAssessmentsUseCase @Inject constructor(
    private val repository: AssessmentRepository,
) {
    operator fun invoke(childId: String): Flow<List<ChildAssessment>> {
        return repository.getChildAssessments(childId)
    }
}
