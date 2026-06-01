package com.misterio.core.data.mapper

import com.misterio.core.domain.model.ConsentType
import com.misterio.core.domain.model.LegalDocument
import com.misterio.core.network.dto.LegalDocumentResponse

internal fun LegalDocumentResponse.toDomain() = LegalDocument(
    type = ConsentType.valueOf(type),
    version = version,
    url = url,
    publishedAt = publishedAt,
)
