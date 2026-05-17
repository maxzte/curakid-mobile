package com.misterio.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

@Composable
fun TrackFab(
    show: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val controller = LocalScaffoldController.current
    val key = rememberSaveable { UUID.randomUUID().toString() }

    DisposableEffect(show, icon, onClick) {
        controller.registerFab(
            key,
            config = FabConfiguration(show = show, icon = icon, onClick = onClick)
        )
        onDispose {
            controller.unregisterFab(key)
        }
    }
}
