package com.misterio.core.domain.util

import com.misterio.core.domain.model.LifePeriod
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.max

object PeriodReadinessCalculator {

    fun calculateRequirementMet(
        period: LifePeriod?,
        answeredQuestionIds: Set<UUID>
    ): Pair<Int, Boolean> {
        val totalQuestions = period?.questions?.size ?: 0
        if (totalQuestions == 0) return 0 to true
        val periodQuestionIds = period?.questions?.map { it.id }?.toSet() ?: emptySet()
        val answeredCount = answeredQuestionIds.count { it in periodQuestionIds }
        val targetCount = ceil(totalQuestions * 0.8).toInt()
        val remaining = max(0, targetCount - answeredCount)
        return remaining to (remaining == 0)
    }
}
