package com.misterio.core.domain.model

import com.misterio.core.domain.util.AgeCalculator
import java.time.LocalDate
import java.util.UUID

data class Child(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val isPremature: Boolean,
    val gestationalWeeks: Int?,
    val allergies: List<String>,
    val chronicDiseases: List<String>,
    val pastDiseases: List<String>,
    val specialFeatures: List<String>,
) {
    val chronologicalAgeInMonths: Int
        get() = AgeCalculator.calculateAgeInMonths(birthDate)

    /**
     * Corrected birth date for premature infants (<37 weeks) until 24 months of age.
     * Standard full-term pregnancy is 40 weeks.
     */
    val effectiveBirthDate: LocalDate
        get() = AgeCalculator.calculateEffectiveBirthDate(
            birthDate = birthDate,
            isPremature = isPremature,
            gestationalWeeks = gestationalWeeks
        )

    val ageInMonths: Int
        get() = AgeCalculator.calculateAgeInMonths(effectiveBirthDate)

    val ageInDays: Long
        get() = AgeCalculator.calculateAgeInDays(effectiveBirthDate)
}
