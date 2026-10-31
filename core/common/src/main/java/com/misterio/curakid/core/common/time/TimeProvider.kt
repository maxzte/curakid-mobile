package com.misterio.curakid.core.common.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

interface TimeProvider {
    fun now(): LocalDateTime
    fun today(): LocalDate
    fun currentTimeMillis(): Long
    fun nowOffset(): OffsetDateTime
}
