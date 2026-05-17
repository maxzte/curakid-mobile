package com.misterio.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.UUID

@Composable
fun TrackTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit = {},
    actions: List<Action> = emptyList(),
) {
    val controller = LocalScaffoldController.current
    val key = rememberSaveable { UUID.randomUUID().toString() }

    DisposableEffect(title, showBack, actions) {
        controller.registerTopBar(
            key,
            config = TopBarConfiguration(
                show = true,
                showBack = showBack,
                title = title,
                onBack = onBack,
                actions = actions
            )
        )
        onDispose {
            controller.unregisterTopBar(key)
        }
    }
}
