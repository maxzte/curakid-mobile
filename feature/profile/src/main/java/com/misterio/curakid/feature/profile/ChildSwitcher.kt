package com.misterio.curakid.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.Child
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun ChildSwitcher(
    children: List<Child>,
    selectedId: UUID?,
    onSelect: (UUID) -> Unit,
    onAddChild: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AddChildItem(onClick = onAddChild)
        }

        if (children.isNotEmpty()) {
            items(children) { child ->
                ChildAvatarItem(
                    child = child,
                    isSelected = child.id == selectedId,
                    onClick = { onSelect(child.id) }
                )
            }
        }
    }
}

@Composable
fun ChildAvatarItem(
    child: Child,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .then(
                    if (isSelected) Modifier.border(2.dp, CurakidTheme.colors.primary, CircleShape)
                    else Modifier
                )
                .padding(if (isSelected) 3.dp else 0.dp)
                .clip(CircleShape)
                .background(if (isSelected) CurakidTheme.colors.primary else CurakidTheme.colors.outlineVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = child.initials,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else CurakidTheme.colors.onBackground
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = child.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) CurakidTheme.colors.primary else CurakidTheme.colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AddChildItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .border(1.5.dp, CurakidTheme.colors.outline, CircleShape)
                .background(CurakidTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Додати дитину",
                tint = CurakidTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            "Додати",
            fontSize = 11.sp,
            color = CurakidTheme.colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
