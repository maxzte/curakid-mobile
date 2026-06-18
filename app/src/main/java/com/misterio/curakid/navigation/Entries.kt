package com.misterio.curakid.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.misterio.curakid.Route
import com.misterio.curakid.core.common.PasscodeMode
import com.misterio.curakid.feature.calendar.CalendarScreen
import com.misterio.curakid.feature.chat.ChatScreen
import com.misterio.curakid.feature.development.ChildDetailsScreen
import com.misterio.curakid.feature.development.DevelopmentScreen
import com.misterio.curakid.feature.home.AiReportScreen
import com.misterio.curakid.feature.home.HomeScreen
import com.misterio.curakid.feature.home.UpcomingEventsScreen
import com.misterio.curakid.feature.home.VaccinationDetailScreen
import com.misterio.curakid.feature.profile.AddChildScreen
import com.misterio.curakid.feature.profile.ProfileScreen
import com.misterio.curakid.feature.profile.SettingsScreen
import java.util.UUID

fun EntryProviderScope<NavKey>.homeEntry(
    onEventClick: (UUID) -> Unit,
    onVaccinationClick: (UUID) -> Unit,
    onShowAllUpcoming: () -> Unit,
    onChatClick: () -> Unit,
    onAiReportClick: (UUID) -> Unit,
    onBack: () -> Unit,
) {
    entry<Route.HomeRoot> {
        HomeScreen(
            onEventClick = onEventClick,
            onVaccinationClick = onVaccinationClick,
            onShowAllUpcoming = onShowAllUpcoming,
            onChatClick = onChatClick,
            onAiReportClick = onAiReportClick
        )
    }
    entry<Route.CalendarFromHome> { key ->
        CalendarScreen(
            eventId = key.eventId,
            onVaccinationClick = onVaccinationClick
        )
    }
    entry<Route.VaccinationDetail> { key ->
        VaccinationDetailScreen(
            vaccineEventId = key.id,
            onBack = onBack,
        )
    }
    entry<Route.UpcomingEvents> {
        UpcomingEventsScreen(
            onEventClick = onEventClick,
            onBack = onBack,
        )
    }
}

fun EntryProviderScope<NavKey>.calendarEntry(
    onVaccinationClick: (UUID) -> Unit,
    onChatClick: () -> Unit,
) {
    entry<Route.CalendarRoot> {
        CalendarScreen(
            eventId = null,
            onVaccinationClick = onVaccinationClick,
            onChatClick = onChatClick
        )
    }
}

fun EntryProviderScope<NavKey>.profileEntry(
    onAddChild: () -> Unit,
    onSettingsOpen: () -> Unit,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onNavigateToPasscode: (PasscodeMode) -> Unit,
) {
    entry<Route.ProfileRoot> {
        ProfileScreen(
            onAddChild = onAddChild,
            onSettingsOpen = onSettingsOpen,
            onChatClick = onChatClick
        )
    }
    entry<Route.Settings> {
        SettingsScreen(
            onBack = onBack,
            onNavigateToPasscode = onNavigateToPasscode
        )
    }
    entry<Route.AddChild> {
        AddChildScreen(onBack = onBack)
    }
}

fun EntryProviderScope<NavKey>.growthEntry(
    onAddMeasurements: (List<String>) -> Unit,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
) {
    entry<Route.GrowthRoot> {
        DevelopmentScreen(
            onAddMeasurements = { lifePeriods ->
                onAddMeasurements(lifePeriods)
            },
            onChatClick = onChatClick
        )
    }
    entry<Route.ChildDetails> {
        ChildDetailsScreen(
            onBack = onBack,
            lifePeriods = it.lifePeriods,
            onViewAiReport = { }
        )
    }
    entry<Route.AiReport> { key ->
        AiReportScreen(
            reportId = key.reportId,
            onBack = onBack
        )
    }
}

fun EntryProviderScope<NavKey>.commonEntries(
    onBack: () -> Unit
) {
    entry<Route.Chat> {
        ChatScreen(
            onBack = onBack
        )
    }
}

