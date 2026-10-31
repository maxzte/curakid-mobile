@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Event
import com.misterio.core.ui.BOT_BAR_H
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
            WindowInsetsSides.Horizontal
        ),
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
                    topPadding = innerPadding.calculateTopPadding()
                )
            }
            is UiState.Error -> {
                ErrorView(
                    error = state.error,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                )
            }
        }
    }
}

@Composable
fun UpcomingEventsContent(
    events: List<Event>,
    onEventClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp
) {
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomPadding = navBarBottom + BOT_BAR_H + CurakidTheme.paddings.medium

    if (events.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding),
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
            contentPadding = PaddingValues(
                start = CurakidTheme.paddings.medium,
                end = CurakidTheme.paddings.medium,
                top = topPadding + CurakidTheme.paddings.medium,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small)
        ) {
            itemsIndexed(
                items = events,
                key = { _, item -> item.id },
                contentType = { _, _ -> "upcoming_event" }
            ) { index, event ->
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EventListItem(
        event = event,
        onClick = onClick,
        dotSize = 10.dp,
        padding = 12.dp,
        modifier = modifier
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
