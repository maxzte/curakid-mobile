package com.misterio.curakid.feature.development

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.PeriodReadiness
import com.misterio.core.ui.components.CuraKidAlertDialog
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun AgeTransitionDialog(
    readiness: PeriodReadiness?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    CuraKidAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
        title = "Новий етап!",
        confirmButtonText = "Завершити",
        dismissButtonText = "Дозаповнити",
        confirmButtonColor = CurakidTheme.colors.primary
    ) {
        if (readiness != null) {
            TransitionReadinessContent(
                readiness = readiness,
                message = if (readiness.isReady)
                    "Ваша дитина вже на новому етапі розвитку! Бажаєте завершити поточний період та отримати звіт?"
                else
                    "Ваша дитина вже на новому етапі, але даних за минулий період ще недостатньо для точного аналізу."
            )
        } else {
            Text(
                text = "Ваша дитина вже на новому етапі розвитку! Бажаєте завершити поточний період та отримати звіт?",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EarlyTransitionDialog(
    readiness: PeriodReadiness?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val canTransitionEarly = readiness?.canTransitionEarly ?: false
    val hasAgeRestriction = readiness?.reasons?.any { it.contains("занадто мала") } ?: false
    val hasUnclosedPreceding = readiness?.reasons?.any { it.contains("ще не завершено") } ?: false

    val title = when {
        canTransitionEarly -> "Ранній перехід"
        hasAgeRestriction || hasUnclosedPreceding -> "Перехід недоступний"
        else -> "Недостатньо даних"
    }

    CuraKidAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = { if (canTransitionEarly) onConfirm() else onDismiss() },
        title = title,
        confirmButtonText = if (canTransitionEarly) "Так, продовжити" else "Зрозуміло",
        dismissButtonText = if (canTransitionEarly) "Скасувати" else "",
        confirmButtonColor = CurakidTheme.colors.primary
    ) {
        if (readiness != null) {
            val unclosedReason = readiness.reasons.firstOrNull { it.contains("ще не завершено") }
            val ageReason = readiness.reasons.firstOrNull { it.contains("занадто мала") }

            val message = when {
                unclosedReason != null -> unclosedReason
                ageReason != null -> "$ageReason Перехід на цей етап можливий після досягнення відповідного віку."
                canTransitionEarly -> "Ви ще не досягли необхідного віку або не відповіли на всі питання. Бажаєте продовжити?"
                else -> "Даних ще занадто мало для проведення аналізу. Будь ласка, дайте відповіді на більше запитань, щоб отримати звіт."
            }

            TransitionReadinessContent(
                readiness = readiness,
                message = message
            )
        } else {
            Text(
                text = "Ви ще не досягли необхідного віку або не відповіли на всі питання. Бажаєте продовжити?",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TransitionReadinessContent(
    readiness: PeriodReadiness,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Загальний прогрес",
                    style = CurakidTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${readiness.completionPercent}% / ${readiness.requiredPercent}%",
                    style = CurakidTheme.typography.labelSmall
                )
            }

            LinearProgressIndicator(
                progress = { readiness.completionPercent.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (readiness.isReady) CurakidTheme.colors.secondary else CurakidTheme.colors.primary,
                trackColor = CurakidTheme.colors.outlineVariant,
                strokeCap = StrokeCap.Round
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val groupedStatus = readiness.domainStatus
                .groupBy { MilestoneCategory.fromDomain(it.domain) }

            groupedStatus.forEach { (category, domains) ->
                val avgCompletion = domains.map { it.completionPercent }.average().toInt()
                val allReady = domains.all { it.isReady }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CurakidTheme.Radius.sm)
                            .background(category.getIconBg()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.getIconTint(),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.label,
                            style = CurakidTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        LinearProgressIndicator(
                            progress = { avgCompletion.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (allReady) CurakidTheme.colors.secondary else category.getIconTint(),
                            trackColor = category.getIconBg(),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    if (allReady) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CurakidTheme.colors.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "$avgCompletion%",
                            style = CurakidTheme.typography.labelSmall.copy(color = CurakidTheme.colors.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
