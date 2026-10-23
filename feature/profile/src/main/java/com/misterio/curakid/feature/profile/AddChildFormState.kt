package com.misterio.curakid.feature.profile

import com.misterio.core.domain.model.Gender

data class AddChildFormState(
    val name: String = "",
    val birthDateMillis: Long = 0L,
    val gender: Gender = Gender.MALE,
    val isPremature: Boolean = false,
    val gestationalWeeks: Int = 0,
    val allergies: List<String> = emptyList(),
    val chronicDiseases: List<String> = emptyList(),
    val pastDiseases: List<String> = emptyList(),
    val specialFeatures: List<String> = emptyList()
) {
    val isGestationalWeeksValid: Boolean
        get() = !isPremature || (gestationalWeeks in 20..37)

    val isFormValid: Boolean
        get() = name.isNotBlank() && birthDateMillis != 0L && isGestationalWeeksValid

    fun getMedicalItems(category: MedicalCategory): List<String> = when (category) {
        MedicalCategory.ALLERGIES -> allergies
        MedicalCategory.CHRONIC -> chronicDiseases
        MedicalCategory.PAST -> pastDiseases
        MedicalCategory.FEATURES -> specialFeatures
    }
}
