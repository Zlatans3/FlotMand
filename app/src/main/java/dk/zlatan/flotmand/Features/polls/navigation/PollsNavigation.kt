package dk.zlatan.flotmand.Features.polls.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.frontpage.datevoting.DateVotingRoute
import dk.zlatan.flotmand.Features.frontpage.datevotingDetail.DateVotingDetailRoute
import dk.zlatan.flotmand.Features.my_events.add_new_event.AddEventScreen
import dk.zlatan.flotmand.design_system.componenets.PredictiveBackScaleContainer

@Composable
fun PollsNavigation(
    onEventCreated: (String) -> Unit = {},
    viewModel: PollsNavigationViewModel = hiltViewModel(),
) {
    val navigationStack: List<PollsDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key ->
            when (key) {
                PollsDestination.PollsList -> {
                    NavEntry(key) {
                        DateVotingRoute(
                            onDismiss = {},
                            showBackButton = false,
                            onVotingClick = { votingId ->
                                viewModel.navigate(PollsDestination.PollDetail(votingId))
                            },
                        )
                    }
                }

                is PollsDestination.PollDetail -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            DateVotingDetailRoute(
                                votingId = key.votingId,
                                onDismiss = { viewModel.pop() },
                                onCreateEvent = { votingId ->
                                    viewModel.navigate(PollsDestination.AddEventFromVoting(votingId))
                                },
                            )
                        }
                    }
                }

                is PollsDestination.AddEventFromVoting -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            AddEventScreen(
                                votingId = key.votingId,
                                onDismiss = { viewModel.pop() },
                                onEventCreated = { eventId ->
                                    viewModel.resetToRoot()
                                    onEventCreated(eventId)
                                },
                            )
                        }
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
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}
