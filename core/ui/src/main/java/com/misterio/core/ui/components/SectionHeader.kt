package com.misterio.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth(),
        headlineContent = {
            Text(
                text = title,
                style = CurakidTheme.typography.titleMedium,
                color = CurakidTheme.colors.onBackground
            )
        },
        trailingContent = {
            if (actionLabel.isNotEmpty()) {
                TextButton(
                    content = {
                        Text(
                            text = actionLabel,
                            style = CurakidTheme.typography.labelLarge,
                            color = CurakidTheme.colors.primary,
                        )
                    },
                    onClick = onAction,
                )
            }
        },
        // Видаляємо стандартні фони та відступи, якщо потрібно,
        // щоб він виглядав саме як заголовок секції
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}
