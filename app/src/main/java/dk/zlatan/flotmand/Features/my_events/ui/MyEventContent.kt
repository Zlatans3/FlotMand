package dk.zlatan.flotmand.Features.my_events.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.util.DanishDateFormatter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The scrollable body of the My Events screen.
 *
 * Uses [AnimatedContent] to slide between the Upcoming (tab 0) and Past (tab 1)
 * event lists.  Each tab has its own empty-state UI so the user always gets
 * meaningful feedback regardless of which list is populated.
 *
 * @param upcomingEvents  Events with status UPCOMING / ONGOING, sorted by date.
 * @param pastEvents      Events with status COMPLETED, sorted newest-first.
 * @param selectedTab     0 = Upcoming, 1 = Past.
 * @param modifier        Applied to the root [AnimatedContent] container.
 * @param publishers      Map of publisherId → [User] for avatar / name display.
 * @param onEventClick    Callback invoked with the eventId on card tap.
 */
@Composable
internal fun MyEventContent(
    upcomingEvents: List<Event>,
    pastEvents: List<Event>,
    selectedTab: Int,
    modifier: Modifier = Modifier,
    publishers: Map<String, User> = emptyMap(),
    isSelectionMode: Boolean = false,
    selectedEventIds: Set<String> = emptySet(),
    onEventClick: (String) -> Unit = {},
    onEventLongClick: (String) -> Unit = {},
    onSelectionToggle: (String) -> Unit = {},
) {
    val danishFormatter = remember { DanishDateFormatter.getDanishDateFormatter("E 'd.' d MMM") }

    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            // Slide direction mirrors the visual position of the tabs:
            // tapping "Past" (higher index) slides content in from the right.
            if (targetState > initialState) {
                val enterFromRight = slideInHorizontally { w -> w } + fadeIn()
                val exitToLeft = slideOutHorizontally { w -> -w } + fadeOut()
                enterFromRight togetherWith exitToLeft
            } else {
                val enterFromLeft = slideInHorizontally { w -> -w } + fadeIn()
                val exitToRight = slideOutHorizontally { w -> w } + fadeOut()
                enterFromLeft togetherWith exitToRight
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        label = "myEventsTabContent",
    ) { tab ->
        when (tab) {
            // ── Tab 0: Upcoming events ────────────────────────────────────────
            0 -> {
                if (upcomingEvents.isEmpty()) {
                    TabEmptyState(
                        emoji = "📅",
                        title = stringResource(R.string.no_upcoming_events_title),
                        subtitle = stringResource(R.string.no_upcoming_events_subtitle),
                    )
                } else {
                    EventList(
                        events = upcomingEvents,
                        publishers = publishers,
                        formatter = danishFormatter,
                        isSelectionMode = isSelectionMode,
                        selectedEventIds = selectedEventIds,
                        onEventClick = onEventClick,
                        onEventLongClick = onEventLongClick,
                        onSelectionToggle = onSelectionToggle,
                    )
                }
            }

            // ── Tab 1: Past events ────────────────────────────────────────────
            else -> {
                if (pastEvents.isEmpty()) {
                    TabEmptyState(
                        emoji = "🗂️",
                        title = stringResource(R.string.no_past_events_title),
                        subtitle = stringResource(R.string.no_past_events_subtitle),
                    )
                } else {
                    EventList(
                        events = pastEvents,
                        publishers = publishers,
                        formatter = danishFormatter,
                        isSelectionMode = isSelectionMode,
                        selectedEventIds = selectedEventIds,
                        onEventClick = onEventClick,
                        onEventLongClick = onEventLongClick,
                        onSelectionToggle = onSelectionToggle,
                    )
                }
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

/**
 * A scrollable list of [EventCard]s with consistent padding and spacing.
 */
@Composable
private fun EventList(
    events: List<Event>,
    publishers: Map<String, User>,
    formatter: DateTimeFormatter,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    selectedEventIds: Set<String> = emptySet(),
    onEventLongClick: (String) -> Unit = {},
    onSelectionToggle: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(events, key = { it.eventId.orEmpty() }) { event ->
            val formattedDate = event.eventDate
                ?.format(formatter)
                ?.replaceFirstChar { it.uppercase() }
                .orEmpty()
            val publisher = publishers[event.publisherId]
            val eventId = event.eventId.orEmpty()

            EventCard(
                modifier = Modifier.animateItem(),
                userProfilePic = publisher?.photoUrl,
                userName = publisher?.displayName.orEmpty(),
                eventName = event.eventName.orEmpty(),
                eventDate = formattedDate,
                eventTime = event.eventStartTime?.toString().orEmpty(),
                isSelectionMode = isSelectionMode,
                isSelected = eventId in selectedEventIds,
                onClick = {
                    if (isSelectionMode) onSelectionToggle(eventId) else onEventClick(eventId)
                },
                onLongClick = { onEventLongClick(eventId) },
            )
        }
    }
}

/**
 * Centred empty-state placeholder shown when a specific tab has no events.
 */
@Composable
private fun TabEmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Preview data ─────────────────────────────────────────────────────────────

private val contentPreviewPublishers = mapOf(
    "user1" to User(id = "user1", displayName = "Lasse Sandø", email = "lasse@example.com"),
    "user2" to User(id = "user2", displayName = "Gustav Rasslan", email = "gustav@example.com"),
)

private val contentPreviewUpcoming = listOf(
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

private val contentPreviewPast = listOf(
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

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Tab: Upcoming — with events")
@Composable
private fun ContentUpcomingPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventContent(
            upcomingEvents = contentPreviewUpcoming,
            pastEvents = contentPreviewPast,
            selectedTab = 0,
            publishers = contentPreviewPublishers,
        )
    }
}

@Preview(showBackground = true, name = "Tab: Past — with events")
@Composable
private fun ContentPastPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventContent(
            upcomingEvents = contentPreviewUpcoming,
            pastEvents = contentPreviewPast,
            selectedTab = 1,
            publishers = contentPreviewPublishers,
        )
    }
}

@Preview(showBackground = true, name = "Tab: Upcoming — empty state")
@Composable
private fun ContentUpcomingEmptyPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventContent(
            upcomingEvents = emptyList(),
            pastEvents = contentPreviewPast,
            selectedTab = 0,
            publishers = contentPreviewPublishers,
        )
    }
}

@Preview(showBackground = true, name = "Tab: Past — empty state")
@Composable
private fun ContentPastEmptyPreview() {
    FlotMandTheme(dynamicColor = false) {
        MyEventContent(
            upcomingEvents = contentPreviewUpcoming,
            pastEvents = emptyList(),
            selectedTab = 1,
            publishers = contentPreviewPublishers,
        )
    }
}

@Preview(showBackground = true, name = "Tab: interactive switcher")
@Composable
private fun ContentInteractivePreview() {
    var tab by remember { mutableIntStateOf(0) }
    FlotMandTheme(dynamicColor = false) {
        Column(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.PrimaryTabRow(selectedTabIndex = tab) {
                listOf("Kommende", "Tidligere").forEachIndexed { index, label ->
                    androidx.compose.material3.Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(label) },
                    )
                }
            }
            MyEventContent(
                upcomingEvents = contentPreviewUpcoming,
                pastEvents = contentPreviewPast,
                selectedTab = tab,
                publishers = contentPreviewPublishers,
            )
        }
    }
}
