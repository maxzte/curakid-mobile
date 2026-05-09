package com.misterio.curakid

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


@Serializable
sealed interface Route : NavKey {
    // Auth
    @Serializable
    data object Login : Route

    @Serializable
    data object SignUp : Route

    @Serializable
    data object RegisterChild : Route

    // Tabs (Roots)
    @Serializable
    data object HomeRoot : Route

    @Serializable
    data object CalendarRoot : Route

    @Serializable
    data object GrowthRoot : Route

    @Serializable
    data object ProfileRoot : Route

    @Serializable
    data object Settings : Route

    // Details (Growth Stack)
    @Serializable
    data object AddChild : Route

    @Serializable
    data class ChildDetails(val childId: String) : Route

    @Serializable
    data class AiReport(val childId: String) : Route
}

enum class BottomTab(
    val icon: ImageVector,
    val label: String,
    val root: NavKey,
) {
    HOME(Icons.Default.Home, label = "Головна", Route.HomeRoot),
    CALENDAR(Icons.Default.CalendarMonth, label = "Календар", Route.CalendarRoot),
    GROWTH(Icons.Default.Timeline, label = "Зростання", Route.GrowthRoot),
    PROFILE(Icons.Default.AccountCircle, label = "Профіль", Route.ProfileRoot)
}
