package com.misterio.core.domain.model

data class LegalDocument(
    val type: ConsentType,
    val version: String,
    val url: String,
    val publishedAt: String,
)

enum class ConsentType {
    TERMS,
    PRIVACY_POLICY
}
