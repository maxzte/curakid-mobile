@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.components.CuraKidDatePickerDialog
import com.misterio.core.ui.components.CuraKidTimePickerDialog
import com.misterio.core.ui.components.FutureOrTodaySelectableDates
import com.misterio.core.ui.components.toStyle
import com.misterio.core.ui.theme.CurakidTheme
import java.time.format.DateTimeFormatter

@Composable
fun EventCreationSheet(
    state: EventFormState,
    onStateChange: (EventFormState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = state.id != null
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CuraKidDatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            initialDate = state.date.toLocalDate(),
            selectableDates = if (!isEditing) FutureOrTodaySelectableDates else DatePickerDefaults.AllDates,
            onDateSelected = { selectedDate ->
                onStateChange(state.copy(date = selectedDate.atTime(state.date.toLocalTime())))
                showDatePicker = false
                showTimePicker = true
            }
        )
    }

    if (showTimePicker) {
        CuraKidTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            initialTime = state.date.toLocalTime(),
            onTimeSelected = { selectedTime ->
                onStateChange(state.copy(date = state.date.toLocalDate().atTime(selectedTime)))
                showTimePicker = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CurakidTheme.paddings.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) stringResource(R.string.edit_event) else stringResource(R.string.create_event),
                style = CurakidTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isEditing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.is_done),
                        style = CurakidTheme.typography.bodyMedium
                    )

                    Checkbox(
                        checked = state.status == EventStatus.DONE,
                        onCheckedChange = { isChecked ->
                            onStateChange(
                                state.copy(
                                    status = if (isChecked) EventStatus.DONE else EventStatus.PLANNED
                                )
                            )
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.title,
            onValueChange = { onStateChange(state.copy(title = it)) },
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventType.entries.forEach { type ->
                FilterChip(
                    selected = state.type == type,
                    onClick = { onStateChange(state.copy(type = type)) },
                    label = { Text(type.toStyle().label) }
                )
            }
        }

        val isDateEditable = state.status != EventStatus.DONE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = isDateEditable,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showDatePicker = true }
                )
        ) {
            OutlinedTextField(
                value = state.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")),
                onValueChange = { },
                label = { Text(stringResource(R.string.date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = if (isDateEditable) CurakidTheme.colors.onSurface else CurakidTheme.colors.onSurface.copy(alpha = 0.38f),
                    disabledBorderColor = if (isDateEditable) CurakidTheme.colors.outline else CurakidTheme.colors.outline.copy(alpha = 0.38f),
                    disabledLabelColor = if (isDateEditable) CurakidTheme.colors.onSurfaceVariant else CurakidTheme.colors.onSurfaceVariant.copy(alpha = 0.38f),
                    disabledPlaceholderColor = CurakidTheme.colors.onSurfaceVariant,
                )
            )
        }

        if (state.type == EventType.DOCTOR) {
            OutlinedTextField(
                value = state.location ?: "",
                onValueChange = { onStateChange(state.copy(location = it)) },
                label = { Text(stringResource(R.string.location)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.description ?: "",
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = CurakidTheme.Radius.md
        ) {
            Text(if (isEditing) stringResource(R.string.save) else stringResource(R.string.create))
        }

        if (isEditing) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = CurakidTheme.Radius.md,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CurakidTheme.colors.error)
            ) {
                Text(stringResource(R.string.delete))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
