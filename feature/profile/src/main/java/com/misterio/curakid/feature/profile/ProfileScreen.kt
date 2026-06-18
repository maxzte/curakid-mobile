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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.domain.model.Child
import com.misterio.core.ui.components.CuraKidButton
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

enum class MedicalCategory(
    val title: String,
    val getIconBg: @Composable () -> Color,
    val getIconTint: @Composable () -> Color,
    val getChipBg: @Composable () -> Color,
    val getChipText: @Composable () -> Color,
    val suggestions: List<String>
) {
    ALLERGIES(
        title = "Алергії",
        getIconBg = { CurakidTheme.colors.errorContainer },
        getIconTint = { CurakidTheme.colors.error },
        getChipBg = { CurakidTheme.colors.errorContainer },
        getChipText = { CurakidTheme.colors.error },
        suggestions = listOf(
            "Пеніцилін", "Арахіс", "Лактоза", "Глютен",
            "Пилок", "Шерсть тварин", "Укуси комах",
            "Латекс", "Морепродукти", "Яйця", "Мед"
        )
    ),
    CHRONIC(
        title = "Хронічні хвороби",
        getIconBg = { Color(0xFFFAEEDA) }, // AmberBg
        getIconTint = { Color(0xFF633806) }, // AmberText
        getChipBg = { Color(0xFFFAEEDA) },
        getChipText = { Color(0xFF633806) },
        suggestions = listOf(
            "Бронхіальна астма", "Цукровий діабет 1 типу",
            "Атопічний дерматит", "Епілепсія",
            "Целіакія", "Муковісцидоз", "Гіпотиреоз",
            "Аутизм (ASD)", "СДУГ"
        )
    ),
    PAST(
        title = "Перенесені хвороби",
        getIconBg = { CurakidTheme.colors.secondaryContainer },
        getIconTint = { CurakidTheme.colors.secondary },
        getChipBg = { CurakidTheme.colors.secondaryContainer },
        getChipText = { CurakidTheme.colors.secondary },
        suggestions = listOf(
            "Вітряна віспа", "Кір", "Краснуха",
            "Паротит", "Коклюш", "Скарлатина",
            "Мононуклеоз", "Гепатит A", "COVID-19"
        )
    ),
    FEATURES(
        title = "Особливості",
        getIconBg = { Color(0xFFEEEDFE) }, // PurpleBg
        getIconTint = { Color(0xFF3C3489) }, // PurpleText
        getChipBg = { Color(0xFFEEEDFE) },
        getChipText = { Color(0xFF3C3489) },
        suggestions = listOf(
            "Недоношена/ний", "Носить окуляри",
            "Слуховий апарат", "Вегетаріанець",
            "Веган", "Ортодонтичне лікування",
            "Логопедична терапія", "ЛФК"
        )
    )
}

@Composable
fun ProfileScreen(
    onAddChild: () -> Unit = {},
    onSettingsOpen: () -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val children by viewModel.children.collectAsState()
    val selectedChild by viewModel.selectedChild.collectAsState()

    val context = LocalContext.current
    var activeModal by remember { mutableStateOf<MedicalCategory?>(null) }

//    TrackTopBar(
//        title = stringResource(R.string.profile),
//        showBack = false,
//        actions = listOf(
//            Action(
//                icon = Icons.Default.Settings,
//                contentDescription = "Settings icon",
//                onClick = onSettingsOpen
//            )
//        )
//    )

    // Modal
    activeModal?.let { category ->
        selectedChild?.let { child ->
            val currentItems = when (category) {
                MedicalCategory.ALLERGIES -> child.allergies
                MedicalCategory.CHRONIC -> child.chronicDiseases
                MedicalCategory.PAST -> child.pastDiseases
                MedicalCategory.FEATURES -> child.specialFeatures
            }
            MedicalPickerModal(
                category = category,
                selectedItems = currentItems,
                onDismiss = { activeModal = null },
                onConfirm = { updated ->
                    val updatedChild = when (category) {
                        MedicalCategory.ALLERGIES -> child.copy(allergies = updated)
                        MedicalCategory.CHRONIC -> child.copy(chronicDiseases = updated)
                        MedicalCategory.PAST -> child.copy(pastDiseases = updated)
                        MedicalCategory.FEATURES -> child.copy(specialFeatures = updated)
                    }
                    viewModel.updateChild(updatedChild)
                    activeModal = null
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = rememberListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
    ) {
        itemWrapper {
            ProfileHeader(onAction = onSettingsOpen, onChatClick = onChatClick)
        }

        item {
            ChildSwitcher(
                children = children,
                selectedId = selectedChild?.id,
                onSelect = { viewModel.onChildSelected(it) },
                onAddChild = onAddChild
            )
        }

        itemWrapper {
            selectedChild?.let {
                SelectedChildCard(
                    child = it,
                    onSave = { updated -> viewModel.updateChild(updated) }
                )
            }
        }

        selectedChild?.let { child ->
            itemWrapper {
                MedicalSection(
                    category = MedicalCategory.ALLERGIES,
                    items = child.allergies,
                    onRemove = { item ->
                        viewModel.updateChild(child.copy(allergies = child.allergies - item))
                    },
                    onAddClick = { activeModal = MedicalCategory.ALLERGIES }
                )
            }

            itemWrapper {
                MedicalSection(
                    category = MedicalCategory.CHRONIC,
                    items = child.chronicDiseases,
                    onRemove = { item ->
                        viewModel.updateChild(child.copy(chronicDiseases = child.chronicDiseases - item))
                    },
                    onAddClick = { activeModal = MedicalCategory.CHRONIC }
                )
            }

            itemWrapper {
                MedicalSection(
                    category = MedicalCategory.PAST,
                    items = child.pastDiseases,
                    onRemove = { item ->
                        viewModel.updateChild(child.copy(pastDiseases = child.pastDiseases - item))
                    },
                    onAddClick = { activeModal = MedicalCategory.PAST }
                )
            }

            itemWrapper {
                MedicalSection(
                    category = MedicalCategory.FEATURES,
                    items = child.specialFeatures,
                    onRemove = { item ->
                        viewModel.updateChild(child.copy(specialFeatures = child.specialFeatures - item))
                    },
                    onAddClick = { activeModal = MedicalCategory.FEATURES }
                )
            }
        }

        itemWrapper {
            CuraKidButton(
                text = "Вийти",
                onClick = { viewModel.logout(context) },
                icon = Icons.AutoMirrored.Filled.Logout,
            )
        }
    }
}

@Composable
private fun ProfileHeader(onAction: () -> Unit, onChatClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.profile),
            style = CurakidTheme.typography.headlineSmall,
            color = CurakidTheme.colors.onBackground,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onAction
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "settings icon"
                )
            }
        }
    }
}

@Composable
private fun ChildSwitcher(
    children: List<Child>,
    selectedId: UUID?,
    onSelect: (UUID) -> Unit,
    onAddChild: () -> Unit
) {
    LazyRow(
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
private fun ChildAvatarItem(child: Child, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
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
private fun AddChildItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
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

private val Child.initials: String
    get() = name.split(" ").let { parts ->
        val first = parts.firstOrNull()?.firstOrNull() ?: ""
        val last = if (parts.size > 1) parts[1].firstOrNull() ?: "" else ""
        "$first$last"
    }.uppercase()


@Composable
private fun SelectedChildCard(child: Child, onSave: (Child) -> Unit) {
    var isEditing by remember(child.id) { mutableStateOf(false) }
//    var editedName by remember(child.id, isEditing) { mutableStateOf(child.name) }
    var editedName by remember(child.id, isEditing) {
        mutableStateOf(
            TextFieldValue(
                text = child.name,
                selection = TextRange(child.name.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    val dateFormatter =
        remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("uk")) }
    val birthDateString = remember(child.birthDate) { child.birthDate.format(dateFormatter) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                        onSave(child.copy(name = editedName.text))
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

// ─── Medical section ──────────────────────────────────────────────────────────

@Composable
fun MedicalSection(
    category: MedicalCategory,
    items: List<String>,
    onRemove: (String) -> Unit,
    onAddClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(items.isNotEmpty()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // Header row
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

            // Expanded body
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

private val MedicalCategory.icon
    get() = when (this) {
        MedicalCategory.ALLERGIES -> Icons.Default.Warning
        MedicalCategory.CHRONIC -> Icons.Default.MonitorHeart
        MedicalCategory.PAST -> Icons.Default.CheckCircle
        MedicalCategory.FEATURES -> Icons.Default.Star
    }

// ─── Chips ────────────────────────────────────────────────────────────────────

@Composable
fun MedicalChip(
    label: String,
    chipBg: Color,
    chipText: Color,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
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
fun AddChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
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

// ─── Medical picker modal ─────────────────────────────────────────────────────

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
                // Modal header
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

                // Suggestions list
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

                // Custom input
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

                // Confirm button
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    CurakidTheme {
        // Since we can't easily mock the ViewModel here without a lot of setup, 
        // we might want to keep a stateless version for previews if needed.
    }
}
