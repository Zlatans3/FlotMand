package dk.zlatan.flotmand.Features.frontpage.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.frontpage.FrontPageRoute
import dk.zlatan.flotmand.Features.frontpage.datevoting.DateVotingRoute
import dk.zlatan.flotmand.Features.frontpage.datevotingDetail.DateVotingDetailRoute
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.EventDetailScreenRoute
import dk.zlatan.flotmand.Features.my_events.add_new_event.AddEventScreen
import dk.zlatan.flotmand.Features.my_events.add_new_event.EditEventScreen
import kotlinx.coroutines.launch

@Suppress("CyclomaticComplexMethod")
@Composable
fun FrontPageNavigation(viewModel: FrontPageNavigationViewModel = hiltViewModel()) {
    val navigationStack: List<FrontPageDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()
    // Navigation overlay for sub-screens
    val snackbarHostState = SnackbarHostState()
    val scope = rememberCoroutineScope()
    var canExitApp by remember { mutableStateOf(false) }

    BackHandler(enabled = navigationStack.isEmpty()) {
        if (navigationStack.size > 1) {
            scope.launch {
                snackbarHostState.showSnackbar("Press back again to exit")
            }
            canExitApp = true
        }
        if (canExitApp) {
            viewModel.pop()
        }
    }
    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key: FrontPageDestination ->
            when (key) {
                FrontPageDestination.FrontPageScreen -> {
                    NavEntry(key) {
                        FrontPageRoute(
                            onDinnerEventClick = { eventId ->
                                viewModel.navigate(FrontPageDestination.EventDetail(eventId))
                            },
                            onDateVotingClick = {
                                viewModel.navigate(FrontPageDestination.DateVoting)
                            },
                            snackbarHostState = snackbarHostState,
                        )
                    }
                }

                is FrontPageDestination.EventDetail -> {
                    NavEntry(key) {
                        EventDetailScreenRoute(
                            eventId = key.eventId,
                            onDismiss = { viewModel.pop() },
                            onEditEvent = { eventId ->
                                viewModel.navigate(FrontPageDestination.EditEvent(eventId))
                            }
                        )
                    }
                }

                FrontPageDestination.DateVoting -> {
                    NavEntry(key) {
                        DateVotingRoute(
                            modifier = Modifier,
                            onDismiss = { viewModel.pop() },
                            onVotingClick = {
                                viewModel.navigate(FrontPageDestination.VotingDetail(it))
                            },
                        )
                    }
                }

                is FrontPageDestination.VotingDetail -> {
                    NavEntry(key) {
                        DateVotingDetailRoute(
                            modifier = Modifier,
                            onDismiss = { viewModel.pop() },
                            votingId = key.votingId,
                            onCreateEvent = { votingId ->
                                viewModel.navigate(FrontPageDestination.AddEventFromVoting(votingId))
                            },
                        )
                    }
                }

                is FrontPageDestination.AddEventFromVoting -> {
                    NavEntry(key) {
                        AddEventScreen(
                            votingId = key.votingId,
                            onDismiss = { viewModel.pop() },
                        )
                    }
                }

                is FrontPageDestination.EditEvent -> {
                    NavEntry(key) {
                        EditEventScreen(
                            eventId = key.eventId,
                            onDismiss = { viewModel.pop() },
                        )
                    }
                }

                FrontPageDestination.HostRotation -> {
                    NavEntry(key) {
                        // TODO: Zlatan 23/01/2026 Later
                    }
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec =
                        tween<IntOffset>(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing,
                        ),
                ),
                ExitTransition.None,
            )
        },
        popTransitionSpec = {
            ContentTransform(
                EnterTransition.None,
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec =
                        tween<IntOffset>(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing,
                        ),
                ),
            )
        },
        predictivePopTransitionSpec = { progress: Int ->
            ContentTransform(
                EnterTransition.None,
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec =
                        tween<IntOffset>(
                            durationMillis = (400 * (100 - progress) / 100).coerceAtLeast(1),
                            easing = FastOutSlowInEasing,
                        ),
                ),
            )
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator<FrontPageDestination>(),
            ),
    )
}
