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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Event
import com.misterio.core.ui.components.ErrorView
import com.misterio.core.ui.components.EventListItem
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun UpcomingEventsScreen(
    onEventClick: (UUID) -> Unit,
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.upcoming_events)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading, UiState.Initial -> LoadingScreen()
            is UiState.Success -> {
                val events = state.data.upcomingItems
                UpcomingEventsContent(
                    events = events,
                    onEventClick = onEventClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Error -> {
                ErrorView(
                    error = state.error,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun UpcomingEventsContent(
    events: List<Event>,
    onEventClick: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.home_no_upcoming_events),
                style = CurakidTheme.typography.bodyMedium,
                color = CurakidTheme.colors.outline
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(CurakidTheme.paddings.medium),
            verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small)
        ) {
            itemsIndexed(events, key = { _, item -> item.id }) { index, event ->
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
}

@Composable
private fun UpcomingEventItem(
    event: Event,
    onClick: () -> Unit
) {
    EventListItem(
        event = event,
        onClick = onClick,
        dotSize = 10.dp,
        padding = 12.dp
    )
}

@Preview(showBackground = true)
@Composable
fun UpcomingEventsPreview() {
    CurakidTheme {
        UpcomingEventsContent(
            events = emptyList(),
            onEventClick = {}
        )
    }
}
