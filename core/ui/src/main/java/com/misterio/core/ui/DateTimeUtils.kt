package com.misterio.core.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    val UKRAINIAN_LOCALE: Locale = Locale.forLanguageTag("uk")

    val dateTimeEventFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", UKRAINIAN_LOCALE)

    val compactDateTimeFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", UKRAINIAN_LOCALE)

    val fullDateFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", UKRAINIAN_LOCALE)

    val mediumDateFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", UKRAINIAN_LOCALE)

    val dayMonthFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM", UKRAINIAN_LOCALE)

    val shortDateFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy", UKRAINIAN_LOCALE)

    val monthYearFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("LLLL yyyy", UKRAINIAN_LOCALE)

    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(dateTimeEventFormat)

    fun formatCompactDateTime(dateTime: LocalDateTime): String = dateTime.format(compactDateTimeFormat)

    fun formatDate(date: LocalDate): String = date.format(fullDateFormat)

    fun formatMediumDate(date: LocalDate): String = date.format(mediumDateFormat)

    fun formatDayMonth(date: LocalDate): String = date.format(dayMonthFormat)

    fun formatShortDate(date: LocalDate): String = date.format(shortDateFormat)

    fun formatMonthYear(yearMonth: YearMonth): String =
        yearMonth.format(monthYearFormat).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(UKRAINIAN_LOCALE) else it.toString()
        }

    fun formatMonthYear(date: LocalDate): String =
        date.format(monthYearFormat).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(UKRAINIAN_LOCALE) else it.toString()
        }

    /**
     * Formats child age in a human-friendly format (e.g., "3 міс 12 дн" or "15 дн").
     */
    fun formatAgeDetailed(months: Int, days: Int): String {
        return if (months > 0) {
            if (days > 0) "$months міс $days дн" else "$months міс"
        } else {
            "$days дн"
        }
    }

    /**
     * Formats total days into months and days (approximating 30 days per month).
     */
    fun formatAgeDetailed(totalDays: Int): String {
        if (totalDays <= 0) return "0 дн"
        val months = totalDays / 30
        val remainingDays = totalDays % 30
        return formatAgeDetailed(months, remainingDays)
    }
}

fun LocalDateTime.toFormatted(): String = DateTimeUtils.formatDateTime(this)
fun LocalDateTime.toCompactFormatted(): String = DateTimeUtils.formatCompactDateTime(this)
fun LocalDateTime.toFormattedDate(): String = DateTimeUtils.formatDate(this.toLocalDate())
fun LocalDate.toFormatted(): String = DateTimeUtils.formatDate(this)
fun LocalDate.toMediumDate(): String = DateTimeUtils.formatMediumDate(this)
fun LocalDate.toDayMonth(): String = DateTimeUtils.formatDayMonth(this)
fun LocalDate.toShortDate(): String = DateTimeUtils.formatShortDate(this)
fun LocalDate.toMonthYear(): String = DateTimeUtils.formatMonthYear(this)
fun YearMonth.toMonthYear(): String = DateTimeUtils.formatMonthYear(this)
