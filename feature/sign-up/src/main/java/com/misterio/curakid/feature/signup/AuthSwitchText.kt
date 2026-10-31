package com.misterio.curakid.feature.signup

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.misterio.core.ui.theme.CurakidTheme

@Composable
internal fun AuthSwitchText(
    text: String,
    actionText: String,
    onClick: () -> Unit
) {
    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = CurakidTheme.colors.onSurfaceVariant)) { append(text) }
        withStyle(SpanStyle(color = CurakidTheme.colors.primary, fontWeight = FontWeight.Medium)) {
            append(
                actionText
            )
        }
    }
    Text(
        text = annotated,
        style = CurakidTheme.typography.bodySmall,
        modifier = Modifier.clickable { onClick() },
        textAlign = TextAlign.Center
    )
}
