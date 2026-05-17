package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AiRepository
import javax.inject.Inject

class PollReportStatusUseCase @Inject constructor(
    private val aiRepository: AiRepository,
) {
    operator fun invoke(reportId: String) = aiRepository.pollReportStatus(reportId)
}
