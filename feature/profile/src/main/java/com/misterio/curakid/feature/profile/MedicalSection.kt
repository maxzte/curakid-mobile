package com.misterio.curakid.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.ui.theme.CurakidTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalSection(
    category: MedicalCategory,
    items: List<String>,
    onRemove: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(items.isNotEmpty()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(category.getIconBg()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.getIconTint(),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    category.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CurakidTheme.colors.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (items.isNotEmpty()) {
                    Text(
                        "${items.size}",
                        fontSize = 11.sp,
                        color = CurakidTheme.colors.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CurakidTheme.colors.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = CurakidTheme.colors.outlineVariant
                    )
                    Spacer(Modifier.height(10.dp))

                    if (items.isEmpty()) {
                        Text(
                            "Не вказано",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        items.forEach { item ->
                            MedicalChip(
                                label = item,
                                chipBg = category.getChipBg(),
                                chipText = category.getChipText(),
                                onRemove = { onRemove(item) }
                            )
                        }
                        AddChip(onClick = onAddClick)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalChip(
    label: String,
    chipBg: Color,
    chipText: Color,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(chipBg)
            .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = chipText)
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Видалити",
                tint = chipText,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun AddChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CurakidTheme.colors.surfaceContainer)
            .border(1.dp, CurakidTheme.colors.outline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Додати",
            tint = CurakidTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )
        Text("Додати", fontSize = 12.sp, color = CurakidTheme.colors.onSurfaceVariant)
    }
}
