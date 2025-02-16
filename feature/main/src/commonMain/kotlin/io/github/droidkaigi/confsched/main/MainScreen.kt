package io.github.droidkaigi.confsched.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import io.github.droidkaigi.confsched.compose.EventEmitter
import io.github.droidkaigi.confsched.compose.rememberEventEmitter
import io.github.droidkaigi.confsched.main.NavigationType.BottomNavigation
import io.github.droidkaigi.confsched.main.NavigationType.NavigationRail
import io.github.droidkaigi.confsched.main.section.GlassLikeBottomNavigation
import io.github.droidkaigi.confsched.main.strings.MainStrings
import io.github.droidkaigi.confsched.ui.SnackbarMessageEffect
import io.github.droidkaigi.confsched.ui.UserMessageStateHolder
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi

const val mainScreenRoute = "main"

fun NavGraphBuilder.mainScreen(
    windowSize: WindowSizeClass,
    mainNestedGraphStateHolder: MainNestedGraphStateHolder,
    mainNestedGraph: NavGraphBuilder.(mainNestedNavController: NavController, PaddingValues) -> Unit,
) {
    composable(mainScreenRoute) {
        MainScreen(
            windowSize = windowSize,
            mainNestedGraphStateHolder = mainNestedGraphStateHolder,
            mainNestedNavGraph = mainNestedGraph,
        )
    }
}

interface MainNestedGraphStateHolder {
    val startDestination: String

    fun routeToTab(route: String): MainScreenTab?

    fun onTabSelected(
        mainNestedNavController: NavController,
        tab: MainScreenTab,
    )
}

enum class NavigationType {
    BottomNavigation,
    NavigationRail,
}

@Composable
fun MainScreen(
    windowSize: WindowSizeClass,
    mainNestedGraphStateHolder: MainNestedGraphStateHolder,
    mainNestedNavGraph: NavGraphBuilder.(NavController, PaddingValues) -> Unit,
    eventEmitter: EventEmitter<MainScreenEvent> = rememberEventEmitter(),
    uiState: MainScreenUiState = mainScreenPresenter(eventEmitter),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val navigationType: NavigationType =
        when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> BottomNavigation
            WindowWidthSizeClass.Medium -> NavigationRail
            WindowWidthSizeClass.Expanded -> NavigationRail
            else -> BottomNavigation
        }

    SnackbarMessageEffect(
        snackbarHostState = snackbarHostState,
        userMessageStateHolder = uiState.userMessageStateHolder,
    )
    MainScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        navigationType = navigationType,
        routeToTab = mainNestedGraphStateHolder::routeToTab,
        onTabSelected = mainNestedGraphStateHolder::onTabSelected,
        mainNestedNavGraph = mainNestedNavGraph,
    )
}

sealed class IconRepresentation {
    data class Vector(val imageVector: ImageVector) : IconRepresentation()

    @ExperimentalResourceApi
    data class Drawable(val drawableId: DrawableResource) : IconRepresentation()
}

enum class MainScreenTab(
    val icon: IconRepresentation.Vector,
    val label: String,
    val contentDescription: String,
    val testTag: String = "mainScreenTab:$label",
) {
    Timetable(
        icon = IconRepresentation.Vector(Icons.Outlined.CalendarMonth),
        label = MainStrings.Timetable.asString(),
        contentDescription = MainStrings.Timetable.asString(),
    ),

    EventMap(
        icon = IconRepresentation.Vector(Icons.Outlined.Map),
        label = MainStrings.EventMap.asString(),
        contentDescription = MainStrings.EventMap.asString(),
    ),

    Favorite(
        icon = IconRepresentation.Vector(Icons.Outlined.Favorite),
        label = MainStrings.EventMap.asString(),
        contentDescription = MainStrings.EventMap.asString(),
    ),

    About(
        icon = IconRepresentation.Vector(Icons.Outlined.Info),
        label = MainStrings.About.asString(),
        contentDescription = MainStrings.About.asString(),
    ),

    ProfileCard(
        icon = IconRepresentation.Vector(Icons.Outlined.People),
        label = MainStrings.ProfileCard.asString(),
        contentDescription = MainStrings.ProfileCard.asString(),
    ),
    ;

    companion object {
        val size: Int get() = values().size
        fun indexOf(tab: MainScreenTab): Int = values().indexOf(tab)
        fun fromIndex(index: Int): MainScreenTab = values()[index]
    }
}

data class MainScreenUiState(
    val userMessageStateHolder: UserMessageStateHolder,
)

@Composable
fun MainScreen(
    @Suppress("UnusedParameter")
    uiState: MainScreenUiState,
    @Suppress("UnusedParameter")
    snackbarHostState: SnackbarHostState,
    navigationType: NavigationType,
    routeToTab: String.() -> MainScreenTab?,
    onTabSelected: (NavController, MainScreenTab) -> Unit,
    mainNestedNavGraph: NavGraphBuilder.(NavController, PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainNestedNavController = rememberNavController()
    val navBackStackEntry by mainNestedNavController.currentBackStackEntryAsState()
    val currentTab = navBackStackEntry?.destination?.route?.routeToTab()
    Row(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(visible = navigationType == NavigationRail) {
            Column {
                Text(text = "nav rail")
                MainScreenTab.values().forEach { tab ->
                    Button(onClick = { onTabSelected(mainNestedNavController, tab) }) {
                        Text(text = tab.label + " " + (currentTab == tab))
                    }
                }
            }
        }

        val hazeState = remember { HazeState() }

        Scaffold(
            bottomBar = {
                GlassLikeBottomNavigation(
                    hazeState = hazeState,
                    onTabSelected = {
                        onTabSelected(mainNestedNavController, it)
                    },
                )
            },
        ) { padding ->
            val hazeStyle =
                HazeStyle(
                    tint = Color.Black.copy(alpha = .2f),
                    blurRadius = 30.dp,
                )
            NavHost(
                navController = mainNestedNavController,
                startDestination = "timetable",
                modifier =
                Modifier.haze(
                    hazeState,
                    hazeStyle,
                ),
                enterTransition = { materialFadeThroughIn() },
                exitTransition = { materialFadeThroughOut() },
            ) {
                mainNestedNavGraph(mainNestedNavController, padding)
            }
        }
    }
}

private fun materialFadeThroughIn(): EnterTransition =
    fadeIn(
        animationSpec =
        tween(
            durationMillis = 195,
            delayMillis = 105,
            easing = LinearOutSlowInEasing,
        ),
    ) +
        scaleIn(
            animationSpec =
            tween(
                durationMillis = 195,
                delayMillis = 105,
                easing = LinearOutSlowInEasing,
            ),
            initialScale = 0.92f,
        )

private fun materialFadeThroughOut(): ExitTransition =
    fadeOut(
        animationSpec =
        tween(
            durationMillis = 105,
            delayMillis = 0,
            easing = FastOutLinearInEasing,
        ),
    )
