@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.theme.CurakidTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.ui.toFormatted
import java.util.UUID

import androidx.compose.ui.res.stringResource
import com.misterio.curakid.feature.home.R

@Composable
fun UpcomingEventsScreen(
    onEventClick: (UUID) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.upcoming_events)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading, UiState.Initial -> LoadingScreen()
            is UiState.Success -> {
                val events = state.data.upcomingItems
                if (events.isNotEmpty()) {
                    UpcomingEventsContent(
                        events = events,
                        onEventClick = onEventClick,
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Майбутніх подій немає")
                    }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Помилка завантаження")
                }
            }
        }
    }
}

@Composable
private fun UpcomingEventsContent(
    events: List<Event>,
    onEventClick: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(CurakidTheme.paddings.medium),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small)
    ) {
        itemsIndexed(events) { index, event ->
            UpcomingEventItem(
                event = event,
                onClick = { onEventClick(event.id) }
            )
            if (index < events.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = CurakidTheme.colors.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventItem(
    event: Event,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val dotColor = when (event.type) {
            EventType.VACCINE -> CurakidTheme.colors.primary
            EventType.DOCTOR -> CurakidTheme.colors.secondary
            EventType.USER -> CurakidTheme.colors.tertiary
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = CurakidTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = event.date.toFormatted(),
                style = CurakidTheme.typography.bodySmall,
                color = CurakidTheme.colors.outline
            )
        }
        
        UpcomingTypeBadge(event = event)
    }
}

@Composable
private fun UpcomingTypeBadge(event: Event) {
    val (bg, textColor, text) = when (event.type) {
        EventType.VACCINE -> Triple(
            CurakidTheme.colors.primaryContainer,
            CurakidTheme.colors.primary,
            "Вакцина"
        )
        EventType.DOCTOR -> Triple(
            CurakidTheme.colors.secondaryContainer,
            CurakidTheme.colors.secondary,
            "Лікар"
        )
        EventType.USER -> Triple(
            CurakidTheme.colors.tertiaryContainer,
            CurakidTheme.colors.tertiary,
            "Подія"
        )
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = CurakidTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
