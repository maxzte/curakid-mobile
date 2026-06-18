package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AiRepository
import java.util.UUID
import javax.inject.Inject

class GetChildAiReportsUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke(childId: UUID) = aiRepository.getReports(childId)
}
