package dk.zlatan.flotmand.Features.frontpage.di

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.frontpage.FrontPageRoute
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.EventDetailScreenRoute
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationViewModel

@Suppress("CyclomaticComplexMethod")
@Composable
fun FrontPageNavigation(viewModel: FrontPageNavigationViewModel = hiltViewModel()) {
    val navigationStack: List<FrontPageDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()
    // Navigation overlay for sub-screens
    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key ->
            when (key) {
                FrontPageDestination.FrontPageScreen -> NavEntry(key) {
                    FrontPageRoute(
                        onClickEvent = { eventId ->
                            viewModel.navigate(FrontPageDestination.EventDetail(eventId))
                        }
                    )
                }

                is FrontPageDestination.EventDetail -> NavEntry(key) {
                    EventDetailScreenRoute()
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
                ),
                ExitTransition.None
            )
        },
        popTransitionSpec = {
            ContentTransform(
                EnterTransition.None,
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right
                )
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        )
    )
}


