package com.misterio.curakid

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.misterio.core.domain.util.UUIDSerializer
import com.misterio.curakid.core.common.PasscodeMode
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
sealed interface Route : NavKey {
    // Auth
    @Serializable
    data object Login : Route

    @Serializable
    data object ForgotPassword : Route

    @Serializable
    data object SignUp : Route

    @Serializable
    data class VerifyEmail(val email: String) : Route

    @Serializable
    data object RegisterChild : Route

    // Tabs (Roots)
    @Serializable
    data object HomeRoot : Route

    @Serializable
    data class CalendarRoot(
        @Serializable(UUIDSerializer::class) val eventId: UUID? = null
    ) : Route {
        companion object {
            val DEFAULT = CalendarRoot()
        }
    }

    @Serializable
    data class CalendarFromHome(
        @Serializable(UUIDSerializer::class) val eventId: UUID? = null
    ) : Route

    @Serializable
    data object GrowthRoot : Route

    @Serializable
    data object ProfileRoot : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class VaccinationDetail(@Serializable(UUIDSerializer::class) val id: UUID) : Route

    @Serializable
    data object UpcomingEvents : Route

    // Details (Growth Stack)
    @Serializable
    data object AddChild : Route

    @Serializable
    data class ChildDetails(val lifePeriods: List<String>) : Route

    @Serializable
    data class AiReport(@Serializable(UUIDSerializer::class) val reportId: UUID) : Route

    @Serializable
    data object Chat : Route

    @Serializable
    data class LegalDocument(val url: String, val title: String) : Route

    @Serializable
    data class Passcode(val mode: PasscodeMode) : Route
}

enum class BottomTab(
    val icon: ImageVector,
    val label: String,
    val root: Route,
) {
    HOME(Icons.Default.Home, label = "Головна", Route.HomeRoot),
    CALENDAR(Icons.Default.CalendarMonth, label = "Календар", Route.CalendarRoot.DEFAULT),
    GROWTH(Icons.Default.Timeline, label = "Зростання", Route.GrowthRoot),
    PROFILE(Icons.Default.AccountCircle, label = "Профіль", Route.ProfileRoot)
}
