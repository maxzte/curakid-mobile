package com.misterio.core.database.entity

import androidx.room.Entity

@Entity(tableName = "chart_norms", primaryKeys = ["chartType", "gender", "x"])
data class ChartNormEntity(
    val chartType: String,
    val gender: String,
    val x: Double,
    val p01: Double,
    val p1: Double,
    val p3: Double,
    val p5: Double,
    val p10: Double,
    val p15: Double,
    val p25: Double,
    val p50: Double,
    val p75: Double,
    val p85: Double,
    val p90: Double,
    val p95: Double,
    val p97: Double,
    val p99: Double,
    val p999: Double
)
