package com.misterio.core.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector

class ScaffoldController(
    initialFabConfig: FabConfiguration = FabConfiguration(),
    initialTopBarConfig: TopBarConfiguration = TopBarConfiguration()
) {
    val snackbarHostState = SnackbarHostState()

    private val topBarStack = mutableStateListOf<Pair<String, TopBarConfiguration>>()
    private val fabStack = mutableStateListOf<Pair<String, FabConfiguration>>()

    val fabConfig: FabConfiguration
        get() = fabStack.lastOrNull()?.second ?: FabConfiguration()

    val topBarConfig: TopBarConfiguration
        get() = topBarStack.lastOrNull()?.second ?: TopBarConfiguration()


    fun registerFab(key: String, config: FabConfiguration) {
        val idx = fabStack.indexOfFirst { it.first == key }
        if (idx >= 0) fabStack[idx] = key to config
        else fabStack.add(key to config)
    }

    fun unregisterFab(key: String) {
        fabStack.removeAll { it.first == key }
    }

    fun registerTopBar(key: String, config: TopBarConfiguration) {
        val idx = topBarStack.indexOfFirst { it.first == key }
        if (idx >= 0) topBarStack[idx] = key to config
        else topBarStack.add(key to config)
    }

    fun unregisterTopBar(key: String) {
        topBarStack.removeAll { it.first == key }
    }

    suspend fun showSnackbar(message: String) {
        snackbarHostState.showSnackbar(message)
    }
}

val LocalScaffoldController = staticCompositionLocalOf<ScaffoldController> {
    error("No ScaffoldController provided")
}

data class FabConfiguration(
    val show: Boolean = false,
    val icon: ImageVector? = null,
    val disabled: Boolean = false,
    val onClick: () -> Unit = {}
)

data class TopBarConfiguration(
    val show: Boolean = false,
    val title: String = "",
    val showBack: Boolean = false,
    val onBack: () -> Unit = {},
    val actions: List<Action> = emptyList()
)

data class Action(
    val icon: ImageVector,
    val contentDescription: String? = null,
    val onClick: () -> Unit
)