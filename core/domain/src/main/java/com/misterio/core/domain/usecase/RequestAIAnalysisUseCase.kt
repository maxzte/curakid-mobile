package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AiRepository
import javax.inject.Inject

class RequestAIAnalysisUseCase @Inject constructor(
    private val repository: AiRepository
) {
    suspend operator fun invoke(childId: String): Result<String> {
        return repository.getAnalysis(childId)
    }
}
