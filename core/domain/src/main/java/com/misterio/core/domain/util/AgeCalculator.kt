package com.misterio.core.domain.util

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

object AgeCalculator {
    const val DAYS_IN_MONTH_AVERAGE = 30.4375

    /**
     * Calculates age in whole months between birth date and target date.
     */
    fun calculateAgeInMonths(birthDate: LocalDate, targetDate: LocalDate = LocalDate.now()): Int {
        if (targetDate.isBefore(birthDate)) return 0
        return Period.between(birthDate, targetDate).toTotalMonths().toInt()
    }

    /**
     * Calculates age in days between birth date and target date.
     */
    fun calculateAgeInDays(birthDate: LocalDate, targetDate: LocalDate = LocalDate.now()): Long {
        if (targetDate.isBefore(birthDate)) return 0L
        return ChronoUnit.DAYS.between(birthDate, targetDate)
    }

    /**
     * Calculates fractional age in months (e.g., for growth charts).
     */
    fun calculateAgeInMonthsExact(
        birthDate: LocalDate,
        targetDate: LocalDate = LocalDate.now(),
        daysPerMonth: Double = 30.44
    ): Double {
        if (targetDate.isBefore(birthDate)) return 0.0
        val days = ChronoUnit.DAYS.between(birthDate, targetDate).toDouble()
        return days / daysPerMonth
    }

    /**
     * Calculates age broken down into full months and remaining days.
     */
    fun calculateDetailedAge(birthDate: LocalDate, targetDate: LocalDate = LocalDate.now()): Pair<Int, Int> {
        if (targetDate.isBefore(birthDate)) return 0 to 0
        val totalDays = ChronoUnit.DAYS.between(birthDate, targetDate).toInt()
        val months = totalDays / 30
        val remainingDays = totalDays % 30
        return months to remainingDays
    }

    /**
     * Calculates effective (corrected) birth date for premature infants (<37 weeks) until 24 months.
     * Full term pregnancy is considered 40 weeks.
     */
    fun calculateEffectiveBirthDate(
        birthDate: LocalDate,
        isPremature: Boolean,
        gestationalWeeks: Int?,
        currentDate: LocalDate = LocalDate.now()
    ): LocalDate {
        if (isPremature && gestationalWeeks != null && gestationalWeeks in 20..36) {
            val chronologicalMonths = calculateAgeInMonths(birthDate, currentDate)
            if (chronologicalMonths < 24) {
                val daysToAdd = (40 - gestationalWeeks) * 7L
                return birthDate.plusDays(daysToAdd)
            }
        }
        return birthDate
    }
}
