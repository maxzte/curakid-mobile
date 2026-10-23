package com.misterio.curakid.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun MedicalPickerModal(
    category: MedicalCategory,
    selectedItems: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var localSelected by remember { mutableStateOf(selectedItems.toSet()) }
    var customInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .heightIn(max = 560.dp),
            shape = CurakidTheme.Radius.xl,
            colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        category.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = CurakidTheme.colors.onBackground
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(CurakidTheme.colors.surfaceContainer)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Закрити",
                            tint = CurakidTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = CurakidTheme.colors.outlineVariant)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(category.suggestions) { suggestion ->
                        val checked = suggestion in localSelected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    localSelected = if (checked)
                                        localSelected - suggestion
                                    else
                                        localSelected + suggestion
                                }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                suggestion,
                                fontSize = 14.sp,
                                color = if (checked) category.getChipText() else CurakidTheme.colors.onBackground,
                                fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
                            )
                            if (checked) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(category.getChipBg()),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = category.getChipText(),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            thickness = 0.5.dp,
                            color = CurakidTheme.colors.outlineVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CurakidTheme.colors.surfaceContainerLow)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Або введіть власне:",
                        fontSize = 12.sp,
                        color = CurakidTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customInput,
                            onValueChange = { customInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Введіть назву...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = CurakidTheme.Radius.md,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = CurakidTheme.colors.outline,
                                focusedBorderColor = CurakidTheme.colors.primary
                            )
                        )
                        IconButton(
                            onClick = {
                                if (customInput.isNotBlank()) {
                                    localSelected = localSelected + customInput.trim()
                                    customInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CurakidTheme.Radius.md)
                                .background(CurakidTheme.colors.primary)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Додати",
                                tint = Color.White
                            )
                        }
                    }
                }

                Button(
                    onClick = { onConfirm(localSelected.toList()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    shape = CurakidTheme.Radius.md,
                    colors = ButtonDefaults.buttonColors(containerColor = CurakidTheme.colors.primary)
                ) {
                    Text(
                        "Зберегти",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
