package com.misterio.curakid.feature.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.ui.LocalScaffoldController
import com.misterio.core.ui.components.ErrorView
import com.misterio.core.ui.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun EventDetailsSheet(
    eventId: UUID,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel<CalendarViewModel, CalendarViewModel.Factory>(
        key = "event_details_$eventId"
    ) { factory ->
        factory.create(eventId)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldController = LocalScaffoldController.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            onDismiss()
            this@LaunchedEffect.launch {
                scaffoldController.showSnackbar(message.asString(context))
            }
        }
    }

    when (val state = uiState) {
        is UiState.Loading, UiState.Initial -> {
            LoadingScreen()
        }

        is UiState.Error -> {
            ErrorView(
                error = state.error,
                onRetry = { viewModel.refresh() }
            )
        }

        is UiState.Success -> {
            val data = state.data
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                EventCreationSheet(
                    state = data.eventForm,
                    onStateChange = { viewModel.onEventFormChange(it) },
                    onSave = { viewModel.onSaveEvent() },
                    onDelete = { viewModel.onDeleteEvent() }
                )
            }
        }
    }
}
