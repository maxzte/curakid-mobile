package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AiRepository
import javax.inject.Inject

class GetChildAiReportsUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke(childId: String) = aiRepository.getReports(childId)
}
