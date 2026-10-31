@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CurakidTheme.colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(category.getIconBg()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.getIconTint(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        category.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CurakidTheme.colors.onBackground
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = CurakidTheme.colors.outlineVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    items = category.suggestions,
                    key = { it },
                    contentType = { "suggestion" }
                ) { suggestion ->
                    val checked = suggestion in localSelected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                localSelected = if (checked) {
                                    localSelected - suggestion
                                } else {
                                    localSelected + suggestion
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            suggestion,
                            fontSize = 15.sp,
                            color = if (checked) category.getChipText() else CurakidTheme.colors.onBackground,
                            fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
                        )
                        if (checked) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(category.getChipBg()),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = category.getChipText(),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.5.dp,
                        color = CurakidTheme.colors.outlineVariant
                    )
                }

                // Custom added items that are not in suggestions
                val customSelected = localSelected - category.suggestions.toSet()
                if (customSelected.isNotEmpty()) {
                    items(
                        items = customSelected.toList(),
                        key = { it },
                        contentType = { "custom_item" }
                    ) { customItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    localSelected = localSelected - customItem
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                customItem,
                                fontSize = 15.sp,
                                color = category.getChipText(),
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(category.getChipBg()),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = category.getChipText(),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.5.dp,
                            color = CurakidTheme.colors.outlineVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CurakidTheme.colors.surfaceContainerLow)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    "Або введіть власне:",
                    fontSize = 12.sp,
                    color = CurakidTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Введіть назву...", fontSize = 14.sp) },
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
                            .size(48.dp)
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
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                shape = CurakidTheme.Radius.md,
                colors = ButtonDefaults.buttonColors(containerColor = CurakidTheme.colors.primary)
            ) {
                Text(
                    "Зберегти (${localSelected.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
