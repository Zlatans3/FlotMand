package dk.zlatan.flotmand.Features.my_events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.my_events.ui.EventTabBar
import dk.zlatan.flotmand.Features.my_events.ui.MyEventTopBar
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun MyEventScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: MyEventViewModel = hiltViewModel(),
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MyEventTopBar() },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Henter dine events...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "😕",
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            uiState.eventList.isEmpty() -> {
                Box(modifier = modifier.padding(paddingValues)) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ingen events endnu",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Opret dit første event!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ExtendedFloatingActionButton(
                        onClick = onAddEventClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                    ) {
                        Icon(FmIcons.Add, contentDescription = "Add Event")
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.create_event), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            else -> {
                MyEventScreen(
                    modifier = modifier.padding(top = paddingValues.calculateTopPadding()),
                    eventList = uiState.eventList,
                    publishers = uiState.publishers,
                    onAddEventClick = onAddEventClick,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

@Composable
internal fun MyEventScreen(
    modifier: Modifier = Modifier,
    eventList: List<Event>,
    publishers: Map<String, User>,
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
) {
    // Tab state
    val tabTitles = listOf("Kommende", "Tidligere")
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    // Filtering logic
    val now = LocalDateTime.now()
    val filteredEvents = eventList.filter { event ->
        val eventDate = event.eventDate
        val eventTime = event.eventStartTime
        val eventDateTime = if (eventDate != null && eventTime != null) {
            try {
                LocalDateTime.of(eventDate, eventTime)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
        when (selectedTabIndex) {
            1 -> eventDateTime != null && eventDateTime.isBefore(now) && eventDateTime.toLocalDate().isBefore(now.toLocalDate()) // Previous: strictly before today

            0 -> eventDateTime == null || !eventDateTime.isBefore(now.toLocalDate().atStartOfDay()) // Upcoming: today or future, or unknown

            else -> true
        }
    }

    var filterMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var sortOption by rememberSaveable { mutableStateOf(0) } // 0: Date Asc, 1: Date Desc, 2: Name A-Z, 3: Name Z-A
    val sortOptions = listOf(
        stringResource(R.string.sort_date_asc),
        stringResource(R.string.sort_date_desc),
        stringResource(R.string.sort_name_az),
        stringResource(R.string.sort_name_za)
    )

    val sortedEvents = when (sortOption) {
        0 -> filteredEvents.sortedWith(compareBy({ it.eventDate }, { it.eventName }))
        1 -> filteredEvents.sortedWith(compareByDescending<Event> { it.eventDate }.thenByDescending { it.eventName })
        2 -> filteredEvents.sortedBy { it.eventName }
        3 -> filteredEvents.sortedByDescending { it.eventName }
        else -> filteredEvents
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.matchParentSize()) {
            EventTabBar(
                tabTitles = tabTitles,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
            MyEventContent(
                modifier = Modifier.weight(1f),
                eventList = sortedEvents,
                publishers = publishers,
                onEventClick = onEventClick,
            )
        }
        ExtendedFloatingActionButton(
            onClick = onAddEventClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        ) {
            Icon(FmIcons.Add, contentDescription = "Add Event")
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.create_event), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun MyEventContent(
    modifier: Modifier = Modifier,
    eventList: List<Event>,
    publishers: Map<String, User> = emptyMap(),
    onEventClick: (String) -> Unit = {},
) {
    val danishFormatter = DateTimeFormatter.ofPattern("E 'd.' d MMM", Locale("da", "DK"))
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(eventList) { event ->
            val publisher = publishers[event.publisherId]
            val formattedDate = event.eventDate?.format(danishFormatter)?.replaceFirstChar { it.uppercase() } ?: ""
            EventCard(
                modifier = Modifier.animateItem(),
                userProfilePic = publisher?.photoUrl,
                userName = publisher?.displayName ?: "Ukendt bruger",
                eventName = event.eventName ?: "Intet navn",
                eventDate = formattedDate,
                eventTime = event.eventStartTime?.toString() ?: "",
                onClick = {
                    event.eventId?.let { onEventClick(it) }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventScreenPreview() {
    val mockPublishers =
        mapOf(
            "publisher1" to
                User(
                    id = "publisher1",
                    displayName = "John Doe",
                    email = "john@example.com",
                ),
        )
    MyEventContent(
        modifier = Modifier,
        eventList = Event.previewEvents(2),
        publishers = mockPublishers,
    )
}
