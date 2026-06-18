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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Gender
import com.misterio.core.ui.components.CuraKidButton
import com.misterio.core.ui.components.CuraKidTextField
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddChildScreen(
    onBack: () -> Unit,
    showBack: Boolean = true
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                ProfileViewModel.UiEvent.NavigateBack -> onBack()
            }
        }
    }

    var name by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableLongStateOf(0L) }
    var gender by rememberSaveable { mutableStateOf(Gender.MALE) }
    var isPremature by rememberSaveable { mutableStateOf(false) }
    var gestationalWeeks by rememberSaveable { mutableIntStateOf(0) }

    var allergies by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var chronicDiseases by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var pastDiseases by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var specialFeatures by rememberSaveable { mutableStateOf(emptyList<String>()) }

    var activeModal by remember { mutableStateOf<MedicalCategory?>(null) }

    val selectableDates = remember {
        object : SelectableDates {
            private val todayUtc = LocalDate.now(ZoneId.of("UTC"))
            private val startMillis =
                todayUtc.minusYears(6).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            private val endMillis =
                todayUtc.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            private val currentYear = LocalDate.now().year

            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in startMillis..endMillis
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in (currentYear - 6)..currentYear
            }
        }
    }
    val datePickerState = rememberDatePickerState(selectableDates = selectableDates)
    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    val onDismiss: () -> Unit = {
        showDatePickerDialog = false
    }

    activeModal?.let { category ->
        val currentItems = when (category) {
            MedicalCategory.ALLERGIES -> allergies
            MedicalCategory.CHRONIC -> chronicDiseases
            MedicalCategory.PAST -> pastDiseases
            MedicalCategory.FEATURES -> specialFeatures
        }
        MedicalPickerModal(
            category = category,
            selectedItems = currentItems,
            onDismiss = { activeModal = null },
            onConfirm = { updated ->
                when (category) {
                    MedicalCategory.ALLERGIES -> allergies = updated
                    MedicalCategory.CHRONIC -> chronicDiseases = updated
                    MedicalCategory.PAST -> pastDiseases = updated
                    MedicalCategory.FEATURES -> specialFeatures = updated
                }
                activeModal = null
            }
        )
    }

    Column(
        modifier = Modifier
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
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }

            Text(
                text = "Додати дитину",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        CuraKidTextField(
            value = name,
            onValueChange = { name = it },
            label = "Імʼя"
        )

        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            overlineContent = {
                if (birthDate != 0L) {
                    Text(
                        text = "Дата народження"
                    )
                }
            },
            headlineContent = {
                Text(
                    text = if (birthDate == 0L) {
                        "Оберіть дату народження"
                    } else {
                        Instant.ofEpochMilli(birthDate)
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

        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(
                        onClick = {
                            birthDate = datePickerState.selectedDateMillis ?: 0L
                            onDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Gender.entries.forEach { g ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { gender = g }
                ) {
                    RadioButton(
                        selected = gender == g,
                        onClick = { gender = g }
                    )
                    Text(
                        text = if (g == Gender.MALE) "Хлопчик" else "Дівчинка",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isPremature = !isPremature }
        ) {
            Checkbox(
                checked = isPremature,
                onCheckedChange = { isPremature = it }
            )
            Text(
                text = "Дострокові пологи (передчасно)",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (isPremature) {
            Spacer(modifier = Modifier.height(16.dp))
            CuraKidTextField(
                value = if (gestationalWeeks == 0) "" else gestationalWeeks.toString(),
                onValueChange = {
                    val newValue = it.filter { char -> char.isDigit() }
                    if (newValue.isEmpty()) {
                        gestationalWeeks = 0
                    } else {
                        val weeks = newValue.toIntOrNull() ?: gestationalWeeks
                        if (weeks <= 42) {
                            gestationalWeeks = weeks
                        }
                    }
                },
                label = "Тиждень народження (20-37)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                error = if (isPremature && gestationalWeeks !in 20..37 && gestationalWeeks != 0)
                    "Введіть коректний тиждень (20-37)" else null
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        MedicalSection(
            category = MedicalCategory.ALLERGIES,
            items = allergies,
            onRemove = { item -> allergies = allergies - item },
            onAddClick = { activeModal = MedicalCategory.ALLERGIES }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MedicalSection(
            category = MedicalCategory.CHRONIC,
            items = chronicDiseases,
            onRemove = { item -> chronicDiseases = chronicDiseases - item },
            onAddClick = { activeModal = MedicalCategory.CHRONIC }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MedicalSection(
            category = MedicalCategory.PAST,
            items = pastDiseases,
            onRemove = { item -> pastDiseases = pastDiseases - item },
            onAddClick = { activeModal = MedicalCategory.PAST }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MedicalSection(
            category = MedicalCategory.FEATURES,
            items = specialFeatures,
            onRemove = { item -> specialFeatures = specialFeatures - item },
            onAddClick = { activeModal = MedicalCategory.FEATURES }
        )

        Spacer(modifier = Modifier.height(32.dp))

        CuraKidButton(
            text = "Зберегти",
            loading = state is UiState.Loading,
            enabled = name.isNotBlank() && birthDate != 0L && (!isPremature || (gestationalWeeks in 20..37)),
            onClick = {
                viewModel.addChild(
                    name = name,
                    birthDate = birthDate,
                    gender = gender,
                    isPremature = isPremature,
                    gestationalWeeks = if (isPremature) gestationalWeeks else null,
                    allergies = allergies,
                    chronicDiseases = chronicDiseases,
                    pastDiseases = pastDiseases,
                    specialFeatures = specialFeatures
                )
            },
        )
    }
}
