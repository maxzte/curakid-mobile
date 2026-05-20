package com.misterio.core.domain.model

enum class ChartType { WFA, HFA, BMI, HCFA, WFH }

data class ChartNormPoint(
    val x: Double, // age in days OR height in cm (for WFH)
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
