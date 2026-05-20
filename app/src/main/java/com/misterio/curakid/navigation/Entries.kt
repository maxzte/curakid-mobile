package com.misterio.curakid.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.misterio.curakid.Route
import com.misterio.curakid.feature.calendar.CalendarScreen
import com.misterio.curakid.feature.development.ChildDetailsScreen
import com.misterio.curakid.feature.development.DevelopmentScreen
import com.misterio.curakid.feature.home.HomeScreen
import com.misterio.curakid.feature.home.UpcomingEventsScreen
import com.misterio.curakid.feature.home.VaccinationDetailScreen
import com.misterio.curakid.feature.profile.AddChildScreen
import com.misterio.curakid.feature.profile.ProfileScreen
import com.misterio.curakid.feature.profile.SettingsScreen
import com.misterio.feature.dashboard.AiReportScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.curakid.feature.home.HomeViewModel

fun EntryProviderScope<NavKey>.homeEntry(
    onEventClick: (String) -> Unit,
    onVaccinationClick: (String) -> Unit,
    onShowAllUpcoming: () -> Unit,
    onBack: () -> Unit,
) {
    entry<Route.HomeRoot> {
        HomeScreen(
            onEventClick = onEventClick,
            onVaccinationClick = onVaccinationClick,
            onShowAllUpcoming = onShowAllUpcoming
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
        val viewModel: HomeViewModel = hiltViewModel()
        UpcomingEventsScreen(
            onEventClick = onEventClick,
            onBack = onBack,
            viewModel = viewModel
        )
    }
}

fun EntryProviderScope<NavKey>.calendarEntry(
    onVaccinationClick: (String) -> Unit,
) {
    entry<Route.CalendarRoot> {
        CalendarScreen(
            eventId = null,
            onVaccinationClick = onVaccinationClick
        )
    }
}

fun EntryProviderScope<NavKey>.profileEntry(
    onAddChild: () -> Unit,
    onSettingsOpen: () -> Unit,
    onBack: () -> Unit,
) {
    entry<Route.ProfileRoot> {
        ProfileScreen(
            onAddChild = onAddChild,
            onSettingsOpen = onSettingsOpen
        )
    }
    entry<Route.Settings> {
        SettingsScreen(onBack = onBack)
    }
    entry<Route.AddChild> {
        AddChildScreen(onBack = onBack)
    }
}

fun EntryProviderScope<NavKey>.growthEntry(
    onAddMeasurements: (List<String>) -> Unit,
    onBack: () -> Unit,
) {
    entry<Route.GrowthRoot> {
        DevelopmentScreen(
            onAddMeasurements = { lifePeriods ->
                onAddMeasurements(lifePeriods)
            }
        )
    }
    entry<Route.ChildDetails> {
        ChildDetailsScreen(
            onBack = onBack,
            lifePeriods = it.lifePeriods,
            onViewAiReport = { }
        )
    }
    entry<Route.AiReport> {
        AiReportScreen(
            onBack = {

            }
        )
    }
}

