package com.misterio.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.R
import com.misterio.core.ui.theme.CurakidTheme

data class EventTypeStyle(
    val primaryColor: Color,
    val containerColor: Color,
    val label: String
)

@Composable
fun EventType.toStyle(): EventTypeStyle {
    return when (this) {
        EventType.VACCINE -> EventTypeStyle(
            primaryColor = CurakidTheme.colors.primary,
            containerColor = CurakidTheme.colors.primaryContainer,
            label = stringResource(R.string.event_type_vaccine)
        )
        EventType.DOCTOR -> EventTypeStyle(
            primaryColor = CurakidTheme.colors.secondary,
            containerColor = CurakidTheme.colors.secondaryContainer,
            label = stringResource(R.string.event_type_doctor)
        )
        EventType.USER -> EventTypeStyle(
            primaryColor = CurakidTheme.colors.tertiary,
            containerColor = CurakidTheme.colors.tertiaryContainer,
            label = stringResource(R.string.event_type_user)
        )
    }
}
