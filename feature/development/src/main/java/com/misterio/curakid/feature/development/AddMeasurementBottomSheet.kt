@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.Child
import com.misterio.core.ui.components.CuraKidButton
import com.misterio.core.ui.components.RulerSlider
import com.misterio.feature.development.R
import java.util.Locale
import kotlin.math.floor
import kotlin.math.round

@Composable
fun MeasurementActionSelectionSheet(
    onDismiss: () -> Unit,
    onWeightHeightClick: () -> Unit,
    onHeadCircClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "Що ви хочете внести?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                headlineContent = { Text("Вага та зріст") },
                leadingContent = {
                    Icon(
                        Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = Color(0xFF6200EE)
                    )
                },
                modifier = Modifier.clickable { onWeightHeightClick() }
            )

            ListItem(
                headlineContent = { Text("Обвід голови") },
                leadingContent = {
                    Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF6200EE))
                },
                modifier = Modifier.clickable { onHeadCircClick() }
            )
        }
    }
}

@Composable
fun AddMeasurementBottomSheet(
    child: Child,
    initialWeight: Double? = null,
    initialHeight: Double? = null,
    onDismiss: () -> Unit,
    onDoneClick: (weight: Double?, height: Double?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val weight = initialWeight ?: 0.0
    var weightWhole by rememberSaveable(weight) { mutableDoubleStateOf(floor(weight)) }
    var weightDecimal by rememberSaveable(weight) {
        mutableDoubleStateOf(
            round((weight - floor(weight)) * 10.0) / 10.0
        )
    }

    val height = initialHeight ?: 50.0
    var heightWhole by rememberSaveable(height) { mutableDoubleStateOf(floor(height)) }
    var heightDecimal by rememberSaveable(height) {
        mutableDoubleStateOf(
            round((height - floor(height)) * 10.0) / 10.0
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.child_weight, child.name),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = String.format(Locale.US, "%.1f kg", weightWhole + weightDecimal),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold
                )
            )

            RulerSlider(
                value = weightWhole.toFloat(),
                onValueChange = { weightWhole = round(it.toDouble()) },
                range = 0f..40f,
                step = 1f,
                modifier = Modifier.height(80.dp)
            )

            RulerSlider(
                value = weightDecimal.toFloat(),
                onValueChange = { weightDecimal = it.toDouble() },
                range = 0f..1f,
                step = 0.1f,
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.child_height, child.name),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = String.format(Locale.US, "%.1f cm", heightWhole + heightDecimal),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold
                )
            )

            RulerSlider(
                value = heightWhole.toFloat(),
                onValueChange = { heightWhole = round(it.toDouble()) },
                range = 30f..180f,
                step = 1f,
                modifier = Modifier.height(80.dp)
            )

            RulerSlider(
                value = heightDecimal.toFloat(),
                onValueChange = { heightDecimal = it.toDouble() },
                range = 0f..1f,
                step = 0.1f,
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CuraKidButton(
                text = stringResource(R.string.done),
                onClick = {
                    val w = floor(weightWhole) + weightDecimal
                    val h = floor(heightWhole) + heightDecimal
                    onDoneClick(
                         if (w == 0.0) null else w,
                         if (h == 0.0) null else h,
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHeadCircBottomSheet(
    childName: String,
    prevHeadCirc: Double? = null,
    onDismiss: () -> Unit,
    onDoneClick: (Double) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialValue = prevHeadCirc ?: 40.0
    var headCircWhole by rememberSaveable(initialValue) { mutableDoubleStateOf(floor(initialValue)) }
    var headCircDecimal by rememberSaveable(initialValue) {
        mutableDoubleStateOf(
            round((initialValue - floor(initialValue)) * 10.0) / 10.0
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.child_head_circ, childName),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = String.format(Locale.US, "%.1f cm", headCircWhole + headCircDecimal),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold
                )
            )

            RulerSlider(
                value = headCircWhole.toFloat(),
                onValueChange = { headCircWhole = round(it.toDouble()) },
                range = 30f..60f,
                step = 1f,
                modifier = Modifier.height(80.dp)
            )

            RulerSlider(
                value = headCircDecimal.toFloat(),
                onValueChange = { headCircDecimal = it.toDouble() },
                range = 0f..1f,
                step = 0.1f,
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CuraKidButton(
                text = stringResource(R.string.done),
                onClick = { onDoneClick(floor(headCircWhole) + headCircDecimal) },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
