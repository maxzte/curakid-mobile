package com.misterio.core.data.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun String.toLocalDateTime(): LocalDateTime {
    return Instant.parse(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}
