package com.misterio.curakid.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Gender
import com.misterio.core.ui.components.BirthDateSelectableDates
import com.misterio.core.ui.components.CuraKidButton
import com.misterio.core.ui.components.CuraKidDatePickerDialog
import com.misterio.core.ui.components.CuraKidTextField
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddChildScreen(
    onBack: () -> Unit,
    showBack: Boolean = true,
    viewModel: AddChildViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            onBack()
        }
    }

    AddChildContent(
        formState = formState,
        isLoading = uiState is UiState.Loading,
        onNameChange = { viewModel.onNameChange(it) },
        onBirthDateChange = { viewModel.onBirthDateChange(it) },
        onGenderChange = { viewModel.onGenderChange(it) },
        onPrematureChange = { viewModel.onPrematureChange(it) },
        onGestationalWeeksChange = { viewModel.onGestationalWeeksChange(it) },
        onCategoryItemsChange = { cat, items -> viewModel.onItemsChange(cat, items) },
        onSubmit = { viewModel.submit() },
        onBack = onBack,
        showBack = showBack
    )
}

@Composable
fun AddChildContent(
    formState: AddChildFormState,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onBirthDateChange: (Long) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onPrematureChange: (Boolean) -> Unit,
    onGestationalWeeksChange: (Int) -> Unit,
    onCategoryItemsChange: (MedicalCategory, List<String>) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    showBack: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var activeModal by remember { mutableStateOf<MedicalCategory?>(null) }

    activeModal?.let { category ->
        val currentItems = when (category) {
            MedicalCategory.ALLERGIES -> formState.allergies
            MedicalCategory.CHRONIC -> formState.chronicDiseases
            MedicalCategory.PAST -> formState.pastDiseases
            MedicalCategory.FEATURES -> formState.specialFeatures
        }
        MedicalPickerModal(
            category = category,
            selectedItems = currentItems,
            onDismiss = { activeModal = null },
            onConfirm = { updated ->
                onCategoryItemsChange(category, updated)
                activeModal = null
            }
        )
    }

    if (showDatePickerDialog) {
        val initialLocalDate = if (formState.birthDateMillis != 0L) {
            Instant.ofEpochMilli(formState.birthDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        } else LocalDate.now()

        CuraKidDatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            onDateSelected = { selectedLocalDate ->
                showDatePickerDialog = false
                val millis = selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                onBirthDateChange(millis)
            },
            initialDate = initialLocalDate,
            selectableDates = BirthDateSelectableDates
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = CurakidTheme.paddings.medium) + rememberListContentPadding())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }

            Text(
                text = stringResource(R.string.add_child_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        CuraKidTextField(
            value = formState.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.add_child_name_label)
        )

        Spacer(modifier = Modifier.height(CurakidTheme.paddings.medium))

        ListItem(
            overlineContent = {
                if (formState.birthDateMillis != 0L) {
                    Text(text = stringResource(R.string.add_child_birth_date))
                }
            },
            headlineContent = {
                Text(
                    text = if (formState.birthDateMillis == 0L) {
                        "Оберіть дату народження"
                    } else {
                        Instant.ofEpochMilli(formState.birthDateMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toString()
                    }
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar"
                )
            },
            modifier = Modifier
                .clip(CurakidTheme.shapes.medium)
                .clickable { showDatePickerDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Gender.entries.forEach { g ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onGenderChange(g) }
                ) {
                    RadioButton(
                        selected = formState.gender == g,
                        onClick = { onGenderChange(g) }
                    )
                    Text(
                        text = if (g == Gender.MALE) "Хлопчик" else "Дівчинка"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPrematureChange(!formState.isPremature) }
        ) {
            Checkbox(
                checked = formState.isPremature,
                onCheckedChange = { onPrematureChange(it) }
            )
            Text(
                text = stringResource(R.string.add_child_preterm),
                modifier = Modifier.padding(start = CurakidTheme.paddings.spaceXs)
            )
        }

        if (formState.isPremature) {
            Spacer(modifier = Modifier.height(CurakidTheme.paddings.medium))
            CuraKidTextField(
                value = if (formState.gestationalWeeks == 0) "" else formState.gestationalWeeks.toString(),
                onValueChange = {
                    val newValue = it.filter { char -> char.isDigit() }
                    if (newValue.isEmpty()) {
                        onGestationalWeeksChange(0)
                    } else {
                        val weeks = newValue.toIntOrNull() ?: formState.gestationalWeeks
                        if (weeks <= 42) {
                            onGestationalWeeksChange(weeks)
                        }
                    }
                },
                label = stringResource(R.string.add_child_birth_week),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                error = if (formState.isPremature && formState.gestationalWeeks !in 20..37 && formState.gestationalWeeks != 0)
                    "Введіть коректний тиждень (20-37)" else null
            )
        }

        MedicalCategory.entries.forEach { category ->
            Spacer(modifier = Modifier.height(CurakidTheme.paddings.medium))
            MedicalSection(
                category = category,
                items = formState.getMedicalItems(category),
                onRemove = { item -> onCategoryItemsChange(category, formState.getMedicalItems(category) - item) },
                onAddClick = { activeModal = category }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        CuraKidButton(
            text = stringResource(R.string.save),
            loading = isLoading,
            enabled = formState.isFormValid,
            onClick = onSubmit,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddChildScreenPreview() {
    CurakidTheme {
        AddChildContent(
            formState = AddChildFormState(),
            isLoading = false,
            onNameChange = {},
            onBirthDateChange = {},
            onGenderChange = {},
            onPrematureChange = {},
            onGestationalWeeksChange = {},
            onCategoryItemsChange = { _, _ -> },
            onSubmit = {},
            onBack = {}
        )
    }
}
