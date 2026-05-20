@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.misterio.core.ui.BOT_BAR_H
import com.misterio.core.ui.LocalScaffoldController
import com.misterio.core.ui.ScaffoldController
import com.misterio.core.ui.TopBarConfiguration
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.curakid.feature.calendar.CalendarScreen
import com.misterio.curakid.feature.development.ChildDetailsScreen
import com.misterio.curakid.feature.development.DevelopmentScreen
import com.misterio.curakid.feature.home.HomeScreen
import com.misterio.curakid.feature.profile.AddChildScreen
import com.misterio.curakid.feature.profile.ProfileScreen
import com.misterio.curakid.feature.profile.SettingsScreen
import com.misterio.curakid.feature.signup.LoginScreen
import com.misterio.curakid.feature.signup.SignUpScreen
import com.misterio.curakid.navigation.Navigator
import com.misterio.curakid.navigation.calendarEntry
import com.misterio.curakid.navigation.growthEntry
import com.misterio.curakid.navigation.homeEntry
import com.misterio.curakid.navigation.profileEntry
import com.misterio.curakid.navigation.rememberNavigationState
import com.misterio.feature.dashboard.AiReportScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
//            viewModel.startDestination.value == null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            CurakidTheme(dynamicColor = false) {
                MainNavigation(
                    viewModel = viewModel,
                    onExit = { moveTaskToBack(true) }
                )
            }
        }
    }
}

@Composable
fun MainNavigation(
    viewModel: MainViewModel,
    onExit: () -> Unit = {}
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle(initialValue = true)
    val scaffoldController = remember { ScaffoldController() }

    // Hoist ALL navigation state to the top level
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

    val activeTab by remember {
        derivedStateOf {
            when (navigationState.topLevelRoute) {
                Route.HomeRoot -> BottomTab.HOME
                is Route.CalendarRoot -> BottomTab.CALENDAR
                is Route.CalendarFromHome -> BottomTab.HOME
                Route.GrowthRoot -> BottomTab.GROWTH
                Route.ProfileRoot -> BottomTab.PROFILE
                else -> BottomTab.HOME
            }
        }
    }

    val currentStack = navigationState.backStacks[navigationState.topLevelRoute] ?: emptyList()
    val currentEntry = currentStack.lastOrNull()
    val showBottomBar = currentEntry !is Route.AddChild
            && currentEntry !is Route.RegisterChild

    val entryProvider = entryProvider {
        homeEntry(
            onEventClick = {
                navigator.navigate(
                    Route.CalendarFromHome(
                        eventId = it
                    )
                )
            },
            onVaccinationClick = {
                navigator.navigate(Route.VaccinationDetail(it))
            },
            onShowAllUpcoming = {
                navigator.navigate(Route.UpcomingEvents)
            },
            onBack = {
                navigator.goBack()
            }
        )
        calendarEntry(
            onVaccinationClick = {
                navigator.navigate(Route.VaccinationDetail(it))
            }
        )
        growthEntry(
            onAddMeasurements = {
                navigator.navigate(Route.ChildDetails(it))
            },
            onBack = { navigator.goBack() }
        )
        profileEntry(
            onAddChild = { navigator.navigate(Route.AddChild) },
            onSettingsOpen = { navigator.navigate(Route.Settings) },
            onBack = { navigator.goBack() }
        )
    }

    CompositionLocalProvider(LocalScaffoldController provides scaffoldController) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (startDestination) {
                is Route.Login -> {
                    val authStack = rememberNavBackStack(Route.Login)

                    NavDisplay(
                        backStack = authStack, entryProvider = entryProvider {
                            entry<Route.Login> {
                                LoginScreen(
                                    onNavigateToSignUp = { authStack.add(Route.SignUp) },
                                )
                            }
                            entry<Route.SignUp> {
                                SignUpScreen(
                                    onNavigateToLogIn = { authStack.removeAt(authStack.lastIndex) },
                                )
                            }
                        }
                    )
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
                        floatingActionButton = {
                            val config = scaffoldController.fabConfig
                            if (config.show) {
                                FloatingActionButton(onClick = config.onClick) {
                                    Icon(imageVector = config.icon!!, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
                            WindowInsetsSides.Horizontal
                        ),
                        bottomBar = {
                            AnimatedVisibility(showBottomBar) {
                                AppBottomBar(activeTab) { tab ->
                                    if (activeTab == tab) {
                                        val stack = navigationState.backStacks[navigationState.topLevelRoute]
                                        while ((stack?.size ?: 0) > 1) {
                                            stack?.removeAt(stack.lastIndex)
                                        }
                                    } else {
                                        navigator.navigate(tab.root)
                                    }
                                }
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

                else -> Unit
            }

            OfflineIndicator(isConnected = isConnected)
        }
    }
}

@Composable
private fun OfflineIndicator(isConnected: Boolean) {
    AnimatedVisibility(
        visible = !isConnected,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red.copy(alpha = 0.8f))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Internet Connection",
                color = Color.White,
                style = CurakidTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun AppTopBar(
    config: TopBarConfiguration,
) {
    val backgroundColor = CurakidTheme.colors.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(vertical = CurakidTheme.paddings.small)
    ) {
        Text(
            text = config.title,
            style = CurakidTheme.typography.titleMedium,
            color = CurakidTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Center)
                .padding(horizontal = CurakidTheme.paddings.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (config.showBack) {
                PillButton(
                    onClick = config.onBack,
                )
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            if (config.actions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    config.actions.forEach { action ->
                        IconPillButton(
                            icon = action.icon,
                            onClick = action.onClick,
                            contentDescription = action.contentDescription
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }
        }
    }
}

@Composable
private fun PillButton(
    onClick: () -> Unit,
) {
    val pillColor = CurakidTheme.colors.onSurface.copy(alpha = 0.10f)
    val borderColor = CurakidTheme.colors.onSurface.copy(alpha = 0.18f)

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(pillColor)
            .border(width = Dp.Hairline, color = borderColor, shape = CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(
                horizontal = CurakidTheme.paddings.small,
                vertical = CurakidTheme.paddings.small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = CurakidTheme.colors.onSurface.copy(alpha = 0.92f)
        )
    }
}

@Composable
private fun IconPillButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?
) {
    val pillColor = CurakidTheme.colors.onSurface.copy(alpha = 0.10f)
    val borderColor = CurakidTheme.colors.onSurface.copy(alpha = 0.18f)

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(pillColor)
            .border(width = Dp.Hairline, color = borderColor, shape = CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = CurakidTheme.colors.onSurface.copy(alpha = 0.90f)
        )
    }
}

@Composable
private fun AppBottomBar(
    activeTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    val backgroundColor = CurakidTheme.colors.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        backgroundColor
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .navigationBarsPadding()
        ) {
            // ШАР 1: Ефект скла (Блюр/Напівпрозорість)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        shape = CircleShape
                        clip = true
                    }
                    .background(backgroundColor.copy(alpha = 0.95f)) // Більш насичений фон
                    .border(
                        width = Dp.Hairline,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // ШАР 2: Контент
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(BOT_BAR_H)
                    .padding(horizontal = CurakidTheme.paddings.small),
                horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidTabItem(tab = tab, isSelected = isSelected)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidTabItem(
    tab: BottomTab,
    isSelected: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) CurakidTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) CurakidTheme.colors.primary else Color.Gray,
    )

    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(backgroundColor, CircleShape)
            .padding(
                horizontal = CurakidTheme.paddings.medium,
                vertical = CurakidTheme.paddings.extraSmall
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            modifier = Modifier.size(22.dp),
            tint = contentColor
        )

        Text(
            text = tab.label,
            style = CurakidTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1
        )
    }
}
