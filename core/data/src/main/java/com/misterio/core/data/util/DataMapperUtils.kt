package com.misterio.core.data.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

fun String.toLocalDateTime(): LocalDateTime {
    return runCatching {
        Instant.parse(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }.recoverCatching {
        OffsetDateTime.parse(this)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    }.getOrElse {
        LocalDateTime.parse(this)
    }
}
