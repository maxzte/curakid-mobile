package com.misterio.curakid.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.Child
import com.misterio.core.ui.theme.CurakidTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SelectedChildCard(
    child: Child,
    onSave: (Child) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember(child.id) { mutableStateOf(false) }
    var editedName by remember(child.id, isEditing) {
        mutableStateOf(
            TextFieldValue(
                text = child.name,
                selection = TextRange(child.name.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("uk")) }
    val birthDateString = remember(child.birthDate) { child.birthDate.format(dateFormatter) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.primaryContainer),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CurakidTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    child.initials,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                if (isEditing) {
                    BasicTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = CurakidTheme.colors.onBackground
                        ),
                        cursorBrush = SolidColor(CurakidTheme.colors.primary)
                    )
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    Text(
                        child.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = CurakidTheme.colors.onBackground
                    )
                }

                Text(
                    "Нар. $birthDateString",
                    fontSize = 12.sp,
                    color = CurakidTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = {
                    if (isEditing) {
                        val trimmedName = editedName.text.trim()
                        if (trimmedName.isNotBlank()) {
                            onSave(child.copy(name = trimmedName))
                        }
                        isEditing = false
                    } else {
                        isEditing = true
                    }
                },
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CurakidTheme.colors.surface)
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Зберегти" else "Редагувати",
                    tint = CurakidTheme.colors.primary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
