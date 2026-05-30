package dk.zlatan.flotmand.Features.frontpage.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.frontpage.FrontPageRoute
import dk.zlatan.flotmand.Features.frontpage.datevoting.DateVotingRoute
import dk.zlatan.flotmand.Features.frontpage.datevotingDetail.DateVotingDetailRoute
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.EventDetailScreenRoute
import dk.zlatan.flotmand.Features.frontpage.notifications.NotificationsScreen
import dk.zlatan.flotmand.Features.my_events.add_new_event.AddEventScreen
import dk.zlatan.flotmand.Features.my_events.add_new_event.EditEventScreen
import dk.zlatan.flotmand.design_system.componenets.PredictiveBackScaleContainer

@Suppress("CyclomaticComplexMethod")
@Composable
fun FrontPageNavigation(viewModel: FrontPageNavigationViewModel = hiltViewModel()) {
    val navigationStack: List<FrontPageDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                            onNotificationsClick = {
                                viewModel.navigate(FrontPageDestination.Notifications)
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
                            },
                        )
                    }
                }

                FrontPageDestination.DateVoting -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            DateVotingRoute(
                                modifier = Modifier,
                                onDismiss = { viewModel.pop() },
                                onVotingClick = {
                                    viewModel.navigate(FrontPageDestination.VotingDetail(it))
                                },
                            )
                        }
                    }
                }

                is FrontPageDestination.VotingDetail -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
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
                }

                is FrontPageDestination.AddEventFromVoting -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            AddEventScreen(
                                votingId = key.votingId,
                                onDismiss = { viewModel.pop() },
                                onEventCreated = { eventId ->
                                    viewModel.resetToRoot()
                                    viewModel.navigate(FrontPageDestination.EventDetail(eventId))
                                },
                            )
                        }
                    }
                }

                is FrontPageDestination.EditEvent -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            EditEventScreen(
                                eventId = key.eventId,
                                onDismiss = { viewModel.pop() },
                            )
                        }
                    }
                }

                FrontPageDestination.Notifications -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            NotificationsScreen(
                                onDismiss = { viewModel.pop() },
                                onEventClick = { eventId ->
                                    viewModel.navigate(FrontPageDestination.EventDetail(eventId))
                                },
                                onPollClick = { votingId ->
                                    viewModel.navigate(FrontPageDestination.VotingDetail(votingId))
                                },
                            )
                        }
                    }
                }

                FrontPageDestination.HostRotation -> {
                    NavEntry(key) {
                        // TODO: Zlatan 23/01/2026 Feature not implemented yet
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
            ContentTransform(EnterTransition.None, ExitTransition.None)
        },
        predictivePopTransitionSpec = { _: Int ->
            ContentTransform(EnterTransition.None, ExitTransition.None)
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
    )
}
