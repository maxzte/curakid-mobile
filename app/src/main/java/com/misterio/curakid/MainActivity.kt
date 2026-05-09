package com.misterio.curakid

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.misterio.core.ui.BOT_BAR_H
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.feature.calendar.CalendarScreen
import com.misterio.feature.dashboard.AiReportScreen
import com.misterio.feature.development.ChildDetailsScreen
import com.misterio.feature.development.DevelopmentScreen
import com.misterio.feature.home.HomeScreen
import com.misterio.feature.profile.AddChildScreen
import com.misterio.feature.profile.ProfileScreen
import com.misterio.feature.signup.LoginScreen
import com.misterio.feature.signup.SignUpScreen
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
    val startDestination by viewModel.startDestination.collectAsState()

    if (startDestination is Route.Login || startDestination is Route.RegisterChild) {
        val authStack = rememberNavBackStack(startDestination!!)

        NavDisplay(
            backStack = authStack, entryProvider = entryProvider {
                entry<Route.Login> {
                    LoginScreen(
                        onNavigateToSignUp = { authStack.add(Route.SignUp) }
                    )
                }
                entry<Route.SignUp> {
                    SignUpScreen(
                        onNavigateToLogIn = { authStack.removeAt(authStack.lastIndex) }
                    )
                }
                entry<Route.RegisterChild> {
                    AddChildScreen(
                        onBack = {},
                        showBack = false
                    )
                }
            }
        )
    } else if (startDestination != null) {
        var activeTab by rememberSaveable { mutableStateOf(BottomTab.HOME) }

        val homeStack = rememberNavBackStack(Route.HomeRoot)
        val calendarStack = rememberNavBackStack(Route.CalendarRoot)
        val growthStack = rememberNavBackStack(Route.GrowthRoot)
        val profileStack = rememberNavBackStack(Route.ProfileRoot)

        val currentStack = when (activeTab) {
            BottomTab.HOME -> homeStack
            BottomTab.CALENDAR -> calendarStack
            BottomTab.GROWTH -> growthStack
            BottomTab.PROFILE -> profileStack
        }

        LaunchedEffect(Unit) {
            activeTab = BottomTab.HOME
        }

        val entryProvider = remember {
            entryProvider<NavKey> {
                // Tabs
                entry<Route.HomeRoot> { HomeScreen() }
                entry<Route.CalendarRoot> { CalendarScreen() }
                entry<Route.GrowthRoot> {
                    DevelopmentScreen()
                }
                entry<Route.ProfileRoot> {
                    ProfileScreen(
                        onAddChild = { profileStack.add(Route.AddChild) },
                        onSettingsOpen = { profileStack.add(Route.Settings) }
                    )
                }

                entry<Route.Settings> {
                    com.misterio.feature.profile.SettingsScreen(
                        onBack = { profileStack.removeAt(profileStack.lastIndex) }
                    )
                }

                entry<Route.AddChild> {
                    AddChildScreen(
                        onBack = { profileStack.removeAt(profileStack.lastIndex) },
                    )
                }
                entry<Route.ChildDetails> { entry ->
                    ChildDetailsScreen(
                        onBack = { growthStack.removeAt(growthStack.lastIndex) },
                        onViewAiReport = { growthStack.add(Route.AiReport(entry.childId)) }
                    )
                }
                entry<Route.AiReport> { entry ->
                    AiReportScreen(
                        childId = entry.childId,
                        onBack = { growthStack.removeAt(growthStack.lastIndex) }
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                NavDisplay(
                    modifier = Modifier.fillMaxSize(),
                    backStack = currentStack,
                    entryProvider = entryProvider,
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
                    onBack = {
                        if (currentStack.size > 1) {
                            currentStack.removeAt(currentStack.lastIndex)
                        } else if (activeTab != BottomTab.HOME) {
                            activeTab = BottomTab.HOME
                        } else {
                            onExit()
                        }
                    }
                )

                if (currentStack.lastOrNull() !is Route.AddChild) {
                    AppBottomBar(activeTab) { tab ->
                        if (activeTab == tab) {
                            while (currentStack.size > 1) currentStack.removeAt(currentStack.lastIndex)
                        } else {
                            activeTab = tab
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.AppBottomBar(
    activeTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    val backgroundColor = CurakidTheme.colors.surface

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
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
