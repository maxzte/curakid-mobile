package com.misterio.core.ui

import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val TOP_BAR_H = 96.dp
val BOT_BAR_H = 56.dp

private val dateTimeEventFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")

fun LocalDateTime.toFormatted(): String = this.format(dateTimeEventFormat)
