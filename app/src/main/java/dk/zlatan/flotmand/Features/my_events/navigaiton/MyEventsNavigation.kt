package dk.zlatan.flotmand.Features.my_events.navigaiton

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.EventDetailScreenRoute
import dk.zlatan.flotmand.Features.frontpage.user_details.UserDetailsScreenRoute
import dk.zlatan.flotmand.Features.my_events.MyEventScreenRoute
import dk.zlatan.flotmand.Features.my_events.add_new_event.AddEventScreen
import dk.zlatan.flotmand.Features.my_events.add_new_event.EditEventScreen

@Suppress("CyclomaticComplexMethod")
@Composable
fun MyEventsNavigation(
    onNavigateToAccountInformation: () -> Unit = {},
    viewModel: MyEventsNavigationViewModel = hiltViewModel(),
) {
    val navigationStack: List<MyEventsDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()

    // Navigation overlay for sub-screens
    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key ->
            when (key) {
                MyEventsDestination.MyEvents -> {
                    NavEntry(key) {
                        MyEventScreenRoute(
                            onAddEventClick = {
                                viewModel.navigate(MyEventsDestination.AddEvent)
                            },
                            onEventClick = { eventId ->
                                viewModel.navigate(MyEventsDestination.EventDetail(eventId))
                            },
                        )
                    }
                }

                MyEventsDestination.AddEvent -> {
                    NavEntry(key) {
                        AddEventScreen(
                            onDismiss = { viewModel.pop() },
                            onEventCreated = { eventId ->
                                viewModel.resetToRoot()
                                viewModel.navigate(MyEventsDestination.EventDetail(eventId))
                            },
                        )
                    }
                }

                is MyEventsDestination.EditEvent -> {
                    NavEntry(key) {
                        EditEventScreen(
                            onDismiss = { viewModel.pop() },
                            eventId = key.eventId,
                        )
                    }
                }

                is MyEventsDestination.AddEventFromVoting -> {
                    NavEntry(key) {
                        AddEventScreen(
                            votingId = key.votingId,
                            onDismiss = { viewModel.pop() },
                            onEventCreated = { eventId ->
                                viewModel.resetToRoot()
                                viewModel.navigate(MyEventsDestination.EventDetail(eventId))
                            },
                        )
                    }
                }

                is MyEventsDestination.EventDetail -> {
                    NavEntry(key) {
                        EventDetailScreenRoute(
                            eventId = key.eventId,
                            onDismiss = { viewModel.pop() },
                            onEditEvent = { eventId ->
                                viewModel.navigate(MyEventsDestination.EditEvent(eventId))
                            },
                            onNavigateToAccountInformation = onNavigateToAccountInformation,
                            onUserClick = { userId ->
                                viewModel.navigate(MyEventsDestination.UserDetails(userId))
                            },
                        )
                    }
                }

                is MyEventsDestination.UserDetails -> {
                    NavEntry(key) {
                        UserDetailsScreenRoute(
                            userId = key.userId,
                            onDismiss = { viewModel.pop() },
                            onEventClick = { eventId ->
                                viewModel.navigate(MyEventsDestination.EventDetail(eventId))
                            },
                        )
                    }
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
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
