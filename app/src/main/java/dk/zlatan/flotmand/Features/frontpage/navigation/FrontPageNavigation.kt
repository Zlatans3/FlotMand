package dk.zlatan.flotmand.Features.frontpage.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.frontpage.FrontPageRoute
import dk.zlatan.flotmand.Features.frontpage.datevoting.DateVotingRoute
import dk.zlatan.flotmand.Features.frontpage.datevotingDetail.DateVotingDetailRoute
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.EventDetailScreenRoute
import dk.zlatan.flotmand.Features.my_events.add_new_event.AddEventScreenRoute

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
                        onDinnerEventClick = { eventId ->
                            viewModel.navigate(FrontPageDestination.EventDetail(eventId))
                        },
                        onDateVotingClick = {
                            viewModel.navigate(FrontPageDestination.DateVoting)
                        },
                    )
                }

                is FrontPageDestination.EventDetail -> NavEntry(key) {
                    EventDetailScreenRoute(
                        eventId = key.eventId,
                        onDismiss = { viewModel.pop() }
                    )
                }

                FrontPageDestination.DateVoting -> NavEntry(key) {
                    DateVotingRoute(
                        modifier = Modifier,
                        onVotingClick = {
                            viewModel.navigate(FrontPageDestination.VotingDetail(it))
                        }
                    )
                }

                is FrontPageDestination.VotingDetail -> NavEntry(key) {

                    DateVotingDetailRoute(
                        modifier = Modifier,
                        onDismiss = { viewModel.pop() },
                        votingId = key.votingId,
                        onCreateEvent = { votingId ->
                            viewModel.navigate(FrontPageDestination.AddEventFromVoting(votingId))
                        }
                    )
                }

                is FrontPageDestination.AddEventFromVoting -> NavEntry(key) {
                    AddEventScreenRoute(
                        votingId = key.votingId,
                        onDismiss = { viewModel.pop() }
                    )
                }

                FrontPageDestination.HostRotation -> NavEntry(key) {

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


