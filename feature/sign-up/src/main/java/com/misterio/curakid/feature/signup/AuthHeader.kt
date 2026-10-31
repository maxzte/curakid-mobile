package com.misterio.curakid.feature.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.ui.theme.CurakidTheme

@Composable
internal fun AuthHeader(title: String, subtitle: String) {
    // App logo mark
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CurakidTheme.Radius.xl)
            .background(CurakidTheme.colors.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CK",
            style = CurakidTheme.typography.titleLarge.copy(
                color = CurakidTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        )
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = title,
        style = CurakidTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = subtitle,
        style = CurakidTheme.typography.bodySmall,
        textAlign = TextAlign.Center
    )
}
