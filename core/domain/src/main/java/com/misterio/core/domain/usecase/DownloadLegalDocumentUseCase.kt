package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.LegalDocumentRepository
import javax.inject.Inject

class DownloadLegalDocumentUseCase @Inject constructor(
    private val repository: LegalDocumentRepository
) {
    suspend operator fun invoke(url: String): Result<String> {
        return repository.downloadContent(url)
    }
}
