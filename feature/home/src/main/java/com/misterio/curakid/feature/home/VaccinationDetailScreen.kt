@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.core.ui.toFormatted
import java.util.UUID

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VaccinationDetailScreen(
    vaccineEventId: UUID,
    onBack: () -> Unit,
) {
    val viewModel: VaccineDetailsViewModel =
        hiltViewModel<VaccineDetailsViewModel, VaccineDetailsViewModel.Factory> { factory ->
            factory.create(vaccineEventId)
        }
    val details by viewModel.vaccineDetails.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                PaddingValues(horizontal = CurakidTheme.paddings.medium) + rememberListContentPadding()
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }

            details?.let {
                Text(
                    text = "Про вакцину ${it.title}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        if (details != null) {
            VaccinationDetailContent(
                vaccination = details!!,
            )
        } else {
            LoadingScreen()
        }
    }
}

@Composable
private fun VaccinationDetailContent(
    vaccination: Event.Vaccine,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CurakidTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Vaccines,
                        null,
                        tint = CurakidTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        vaccination.title,
                        style = CurakidTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Доза ${vaccination.doseNumber}/${vaccination.totalDoses}",
                        style = CurakidTheme.typography.bodyMedium,
                        color = CurakidTheme.colors.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            DetailItem(
                icon = Icons.Default.CalendarToday,
                label = "Дата",
                value = vaccination.date.toFormatted()
            )

            Spacer(Modifier.height(16.dp))

            DetailItem(
                icon = Icons.Default.Info,
                label = "Статус",
                value = when (vaccination.status) {
                    EventStatus.DONE -> "Виконано"
                    EventStatus.PLANNED -> "Заплановано"
                    EventStatus.SKIPPED -> "Пропущено"
                },
                valueColor = if (vaccination.status == EventStatus.DONE) Color(0xFF4CAF50) else CurakidTheme.colors.primary
            )
        }
    }

    vaccination.notes?.let {
        Text(
            "Про вакцину",
            style = CurakidTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            it,
            style = CurakidTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = CurakidTheme.colors.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            null,
            tint = CurakidTheme.colors.outline,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = CurakidTheme.typography.labelSmall,
                color = CurakidTheme.colors.outline
            )
            Text(
                value,
                style = CurakidTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
