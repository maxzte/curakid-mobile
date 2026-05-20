package com.misterio.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun ScreenHeader(
    title: String,
    childName: String
) {
    val name = childName.takeIf { it.isNotEmpty() }?.take(1)?.uppercase().orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = CurakidTheme.typography.headlineSmall,
            color = CurakidTheme.colors.onBackground,
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CurakidTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = CurakidTheme.typography.bodyMedium,
                color = CurakidTheme.colors.onSurfaceVariant
            )
        }
    }
}
