package dk.zlatan.flotmand.Features.my_events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.my_events.ui.MyEventContent
import dk.zlatan.flotmand.Features.my_events.ui.MyEventEmptyState
import dk.zlatan.flotmand.Features.my_events.ui.MyEventErrorState
import dk.zlatan.flotmand.Features.my_events.ui.MyEventLoadingState
import dk.zlatan.flotmand.Features.my_events.ui.MyEventTopBar
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import java.time.LocalDate
import java.time.LocalTime

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * Screen entry-point that connects the [MyEventViewModel] to the UI.
 *
 * Tab selection state lives here so it is shared between the top bar
 * (which contains the [SegmentedControl]) and the content body — both
 * are slots of the same [Scaffold] and therefore need to observe the
 * same value.
 */
@Composable
internal fun MyEventScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: MyEventViewModel = hiltViewModel(),
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Tab state: 0 = Upcoming, 1 = Past.
    // Hoisted to the route so it feeds both the top-bar and the content body.
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MyEventTopBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                MyEventLoadingState(modifier = Modifier.padding(paddingValues))
            }

            uiState.errorMessage != null -> {
                MyEventErrorState(
                    errorMessage = uiState.errorMessage!!,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            uiState.upcomingEvents.isEmpty() && uiState.pastEvents.isEmpty() -> {
                MyEventEmptyState(
                    onAddEventClick = onAddEventClick,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            else -> {
                MyEventScreen(
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                    upcomingEvents = uiState.upcomingEvents,
                    pastEvents = uiState.pastEvents,
                    publishers = uiState.publishers,
                    selectedTab = selectedTab,
                    onAddEventClick = onAddEventClick,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * Pure content composable — no ViewModel, fully preview-able.
 *
 * Overlays a persistent [ExtendedFloatingActionButton] on top of the
 * animated [MyEventContent] so the "Create Event" action is always
 * reachable regardless of the active tab.
 *
 * @param upcomingEvents  Pre-filtered list of upcoming/ongoing events.
 * @param pastEvents      Pre-filtered list of completed events.
 * @param publishers      Map of publisherId → [User] for avatar display.
 * @param selectedTab     The tab index driven by the [SegmentedControl] above.
 * @param onAddEventClick Navigate to the add-event flow.
 * @param onEventClick    Navigate to the event detail screen.
 */
@Composable
internal fun MyEventScreen(
    upcomingEvents: List<Event>,
    pastEvents: List<Event>,
    publishers: Map<String, User>,
    selectedTab: Int,
    modifier: Modifier = Modifier,
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        MyEventContent(
            modifier = Modifier.matchParentSize(),
            upcomingEvents = upcomingEvents,
            pastEvents = pastEvents,
            selectedTab = selectedTab,
            publishers = publishers,
            onEventClick = onEventClick,
        )

        ExtendedFloatingActionButton(
            onClick = onAddEventClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(
                imageVector = FmIcons.Add,
                contentDescription = stringResource(R.string.add_event_content_description),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.create_event),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewPublishers = mapOf(
    "user1" to User(id = "user1", displayName = "Lasse Sandø", email = "lasse@example.com"),
    "user2" to User(id = "user2", displayName = "Gustav Rasslan", email = "gustav@example.com"),
)

private val previewUpcoming = listOf(
    Event.create(
        eventId = "e1",
        publisherId = "user1",
        eventName = "Middag hos Lasse",
        location = "Fortuna Alle 12, Frederiksberg",
        eventDate = LocalDate.now().plusDays(3),
        eventStartTime = LocalTime.of(19, 0),
    ),
    Event.create(
        eventId = "e2",
        publisherId = "user2",
        eventName = "Brunch med Gustav",
        location = "Nørrebrogade 45, København",
        eventDate = LocalDate.now().plusDays(14),
        eventStartTime = LocalTime.of(11, 30),
    ),
)

private val previewPast = listOf(
    Event.create(
        eventId = "e3",
        publisherId = "user2",
        eventName = "Frokost hos Gustav",
        location = "Østerbrogade 7, København",
        eventDate = LocalDate.now().minusDays(10),
        eventStartTime = LocalTime.of(12, 0),
    ),
    Event.create(
        eventId = "e4",
        publisherId = "user1",
        eventName = "Julefrokost",
        location = "Vesterbrogade 3, København",
        eventDate = LocalDate.now().minusDays(30),
        eventStartTime = LocalTime.of(13, 0),
    ),
)

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Upcoming tab — with events")
@Composable
private fun MyEventScreenUpcomingPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventScreen(
            upcomingEvents = previewUpcoming,
            pastEvents = previewPast,
            publishers = previewPublishers,
            selectedTab = 0,
        )
    }
}

@Preview(showBackground = true, name = "Past tab — with events")
@Composable
private fun MyEventScreenPastPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventScreen(
            upcomingEvents = previewUpcoming,
            pastEvents = previewPast,
            publishers = previewPublishers,
            selectedTab = 1,
        )
    }
}

@Preview(showBackground = true, name = "Upcoming tab — empty state")
@Composable
private fun MyEventScreenUpcomingEmptyPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventScreen(
            upcomingEvents = emptyList(),
            pastEvents = previewPast,
            publishers = previewPublishers,
            selectedTab = 0,
        )
    }
}

@Preview(showBackground = true, name = "Past tab — empty state")
@Composable
private fun MyEventScreenPastEmptyPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventScreen(
            upcomingEvents = previewUpcoming,
            pastEvents = emptyList(),
            publishers = previewPublishers,
            selectedTab = 1,
        )
    }
}

/**
 * Fully interactive end-to-end preview — tap the pill switch to slide
 * between Upcoming and Past event lists.
 */
@Preview(showBackground = true, name = "Full screen — interactive switcher")
@Composable
private fun MyEventScreenFullInteractivePreview() {
    var selectedTab by remember { mutableIntStateOf(0) }
    FlotMandTheme(dynamicColor = false) {
        androidx.compose.foundation.layout.Column {
            MyEventTopBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
            MyEventScreen(
                upcomingEvents = previewUpcoming,
                pastEvents = previewPast,
                publishers = previewPublishers,
                selectedTab = selectedTab,
            )
        }
    }
}
