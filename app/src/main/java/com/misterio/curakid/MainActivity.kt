@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.misterio.core.data.worker.EventNotificationWorker
import com.misterio.core.ui.LocalScaffoldController
import com.misterio.core.ui.ScaffoldController
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.curakid.core.common.PasscodeMode
import com.misterio.curakid.feature.profile.AddChildScreen
import com.misterio.curakid.feature.profile.PasscodeScreen
import com.misterio.curakid.navigation.AuthNavHost
import com.misterio.curakid.navigation.BottomSheetSceneStrategy
import com.misterio.curakid.navigation.Navigator
import com.misterio.curakid.navigation.calendarEntry
import com.misterio.curakid.navigation.commonEntries
import com.misterio.curakid.navigation.growthEntry
import com.misterio.curakid.navigation.homeEntry
import com.misterio.curakid.navigation.profileEntry
import com.misterio.curakid.navigation.rememberNavigationState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            )
        )

        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel by viewModels()
        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }

        handleNotificationIntent(intent, viewModel)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            CurakidTheme(dynamicColor = false) {
                MainNavigation(
                    viewModel = viewModel,
                    onExit = { moveTaskToBack(true) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val viewModel: MainViewModel by viewModels()
        handleNotificationIntent(intent, viewModel)
    }

    private fun handleNotificationIntent(intent: Intent?, viewModel: MainViewModel) {
        if (intent == null) return

        val eventId = intent.getStringExtra(EventNotificationWorker.EXTRA_EVENT_ID)
            ?: intent.getStringExtra("eventId")
            ?: intent.getStringExtra("event_id")

        if (!eventId.isNullOrBlank()) {
            viewModel.setPendingEventId(eventId)
        }

        val reportId = intent.getStringExtra(EXTRA_REPORT_ID)
            ?: intent.getStringExtra("reportId")
            ?: intent.getStringExtra("report_id")

        if (!reportId.isNullOrBlank()) {
            viewModel.setPendingReportId(reportId)
        }
    }

    companion object {
        const val EXTRA_REPORT_ID = "extra_report_id"
    }
}

@Composable
fun MainNavigation(
    viewModel: MainViewModel,
    onExit: () -> Unit = {}
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle(initialValue = true)
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle(initialValue = false)
    val scaffoldController = remember { ScaffoldController() }
    val pendingEventId by viewModel.pendingEventId.collectAsStateWithLifecycle()
    val pendingReportId by viewModel.pendingReportId.collectAsStateWithLifecycle()

    val navigationState = rememberNavigationState(
        startRoute = BottomTab.HOME.root,
        topLevelRoutes = setOf(
            BottomTab.HOME.root,
            BottomTab.CALENDAR.root,
            BottomTab.GROWTH.root,
            BottomTab.PROFILE.root,
        )
    )
    val navigator = remember { Navigator(navigationState) }

    LaunchedEffect(pendingEventId, startDestination) {
        if (startDestination != null && startDestination !is Route.Login && startDestination !is Route.Passcode) {
            pendingEventId?.let { id ->
                navigator.navigate(Route.CalendarRoot(eventId = id))
                viewModel.clearPendingEventId()
            }
        }
    }

    LaunchedEffect(pendingReportId, startDestination) {
        if (startDestination != null && startDestination !is Route.Login && startDestination !is Route.Passcode) {
            pendingReportId?.let { id ->
                navigator.navigate(Route.AiReport(reportId = id))
                viewModel.clearPendingReportId()
            }
        }
    }

    val activeTab by remember {
        derivedStateOf {
            when (navigationState.topLevelRoute) {
                Route.HomeRoot -> BottomTab.HOME
                is Route.CalendarRoot -> BottomTab.CALENDAR
                Route.GrowthRoot -> BottomTab.GROWTH
                Route.ProfileRoot -> BottomTab.PROFILE
                else -> BottomTab.HOME
            }
        }
    }

    val context = LocalContext.current
    val currentStack = navigationState.backStacks[navigationState.topLevelRoute] ?: emptyList()
    val currentEntry = currentStack.lastOrNull()

    // Secure sensitive screens (medical reports, chat, passcode) against screen recording / recent task snapshots
    LaunchedEffect(currentEntry) {
        val isSensitiveScreen = currentEntry is Route.Passcode ||
                currentEntry is Route.AiReport ||
                currentEntry is Route.Chat
        (context as? android.app.Activity)?.window?.let { window ->
            if (isSensitiveScreen) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
    val showBottomBar = currentEntry !is Route.AddChild
            && currentEntry !is Route.RegisterChild
            && currentEntry !is Route.Chat
            && currentEntry !is Route.Passcode

    val entryProvider = entryProvider {
        homeEntry(
            onEventClick = { navigator.navigate(Route.EventDetails(eventId = it)) },
            onVaccinationClick = { navigator.navigate(Route.VaccinationDetail(it)) },
            onShowAllUpcoming = { navigator.navigate(Route.UpcomingEvents) },
            onChatClick = { navigator.navigate(Route.Chat) },
            onAiReportClick = { reportId -> navigator.navigate(Route.AiReport(reportId)) },
            onBack = { navigator.goBack() }
        )
        calendarEntry(
            onVaccinationClick = { navigator.navigate(Route.VaccinationDetail(it)) },
            onChatClick = { navigator.navigate(Route.Chat) }
        )
        growthEntry(
            onAddMeasurements = { navigator.navigate(Route.ChildDetails(it)) },
            onBack = { navigator.goBack() },
            onChatClick = { navigator.navigate(Route.Chat) }
        )
        profileEntry(
            onAddChild = { navigator.navigate(Route.AddChild) },
            onSettingsOpen = { navigator.navigate(Route.Settings) },
            onBack = { navigator.goBack() },
            onChatClick = { navigator.navigate(Route.Chat) },
            onNavigateToPasscode = { mode -> navigator.navigate(Route.Passcode(mode)) }
        )
        entry<Route.Passcode> {
            PasscodeScreen(
                mode = it.mode,
                onSuccess = {
                    if (it.mode == PasscodeMode.SETUP) {
                        viewModel.onPasscodeSuccess()
                    }
                    navigator.goBack()
                },
                onBack = { navigator.goBack() }
            )
        }
        commonEntries(
            onBack = { navigator.goBack() }
        )
    }

    CompositionLocalProvider(LocalScaffoldController provides scaffoldController) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val destination = startDestination) {
                is Route.Passcode -> {
                    PasscodeScreen(
                        mode = destination.mode,
                        onSuccess = { viewModel.onPasscodeSuccess() },
                        onForgotPasscode = { viewModel.resetApp() }
                    )
                }

                is Route.Login -> {
                    AuthNavHost()
                }

                is Route.RegisterChild -> {
                    AddChildScreen(
                        showBack = false,
                        onBack = {},
                    )
                }

                is Route.HomeRoot -> {
                    Scaffold(
                        topBar = {
                            if (scaffoldController.topBarConfig.show) {
                                AppTopBar(scaffoldController.topBarConfig)
                            }
                        },
                        snackbarHost = { SnackbarHost(scaffoldController.snackbarHostState) },
                        floatingActionButton = {
                            val config = scaffoldController.fabConfig
                            val icon = config.icon
                            if (config.show && icon != null) {
                                FloatingActionButton(onClick = config.onClick) {
                                    Icon(imageVector = icon, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
                            WindowInsetsSides.Horizontal
                        ),
                        bottomBar = {
                            AnimatedVisibility(showBottomBar) {
                                AppBottomBar(
                                    activeTab = activeTab,
                                    onTabSelected = { tab ->
                                        if (activeTab == tab) {
                                            val stack = navigationState.backStacks[navigationState.topLevelRoute]
                                            while ((stack?.size ?: 0) > 1) {
                                                stack?.removeAt(stack.lastIndex)
                                            }
                                        } else {
                                            navigator.navigate(tab.root)
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .padding(
                                    start = innerPadding.calculateStartPadding(
                                        LocalLayoutDirection.current
                                    ),
                                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                                )
                                .fillMaxSize()
                        ) {
                            NavDisplay(
                                entries = navigationState.toDecoratedEntries(entryProvider),
                                sceneStrategies = listOf(
                                    remember { BottomSheetSceneStrategy<NavKey>() },
                                    remember { SinglePaneSceneStrategy<NavKey>() }
                                ),
                                onBack = { navigator.goBack() },
                                transitionSpec = {
                                    val initialTab =
                                        BottomTab.entries.find { it.root.toString() == initialState.key.toString() }
                                    val targetTab =
                                        BottomTab.entries.find { it.root.toString() == targetState.key.toString() }

                                    if (initialTab != null && targetTab != null && initialTab != targetTab) {
                                        val direction =
                                            if (targetTab.ordinal > initialTab.ordinal) 1 else -1
                                        (slideInHorizontally { width -> direction * width } + fadeIn()).togetherWith(
                                            slideOutHorizontally { width -> -direction * width } + fadeOut()
                                        )
                                    } else {
                                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                            slideOutHorizontally { width -> -width } + fadeOut()
                                        )
                                    }
                                },
                                popTransitionSpec = {
                                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> width } + fadeOut()
                                    )
                                },
                            )
                        }
                    }
                }

                null -> {
                    com.misterio.core.ui.components.LoadingScreen()
                }

                else -> Unit
            }

            OfflineIndicator(isConnected = isConnected, isSyncing = isSyncing)
        }
    }
}
