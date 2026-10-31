package com.misterio.curakid.core.common.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun today(): LocalDate = LocalDate.now()
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun nowOffset(): OffsetDateTime = OffsetDateTime.now()
}
