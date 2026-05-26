package com.misterio.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.BasicRichText
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun AiReportCard(
    title: String,
    reportText: String,
    recommendations: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val parser = remember { CommonmarkAstNodeParser() }
    val astNodeReport = remember(reportText) {
        parser.parse(reportText.trimIndent())
    }
    val astNodeRec = remember(recommendations) {
        parser.parse(recommendations.trimIndent())
    }
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CurakidTheme.paddings.medium),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Хедер
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "AI Report",
                    tint = if (isLoading) CurakidTheme.colors.outlineVariant else CurakidTheme.colors.primary
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(20.dp)
                            .shimmerEffect()
                    )
                } else {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = CurakidTheme.colors.outlineVariant.copy(alpha = 0.5f))

            // Тіло: Звіт
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Аналіз:",
                    fontWeight = FontWeight.Bold,
                    color = if (isLoading) CurakidTheme.colors.outlineVariant else CurakidTheme.colors.primary
                )
                if (isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(14.dp).shimmerEffect())
                        Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).shimmerEffect())
                        Box(modifier = Modifier.fillMaxWidth(0.95f).height(14.dp).shimmerEffect())
                    }
                } else {
                    BasicRichText {
                        BasicMarkdown(astNodeReport)
                    }
                }
            }

            // Тіло: Рекомендації
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Рекомендації:",
                    style = CurakidTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isLoading) CurakidTheme.colors.outlineVariant else CurakidTheme.colors.primary
                )
                if (isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(14.dp).shimmerEffect())
                        Box(modifier = Modifier.fillMaxWidth(0.85f).height(14.dp).shimmerEffect())
                    }
                } else {
                    BasicRichText {
                        BasicMarkdown(astNodeRec)
                    }
                }
            }
        }
    }
}
