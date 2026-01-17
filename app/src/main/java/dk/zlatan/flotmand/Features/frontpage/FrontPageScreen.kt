package dk.zlatan.flotmand.Features.frontpage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.ui.FmAnimatableTopBar
import dk.zlatan.flotmand.Features.frontpage.ui.FrontPageNewHeader
import dk.zlatan.flotmand.Features.frontpage.ui.NextEventSection
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.Event.Companion.previewEvents
import dk.zlatan.flotmand.model.User

@Composable
internal fun FrontPageRoute(
    modifier: Modifier = Modifier,
    onDinnerEventClick: (String) -> Unit,
    onDateVotingClick: () -> Unit,
    viewModel: FrontPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        Log.d("FrontPageRoute", "UI State changed - isLoading: ${uiState.isLoading}, eventCount: ${uiState.eventList.size}, error: ${uiState.errorMessage}")
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Henter events...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        uiState.errorMessage != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "😕",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        uiState.eventList.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🎉",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Ingen events endnu",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Vær den første til at oprette et event!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        else -> {
            FrontpageContent(
                modifier = modifier,
                onClickEvent = onDinnerEventClick,
                onDateVotingClick = onDateVotingClick,
                eventList = uiState.eventList,
                previousEvents = uiState.previousEvents,
                publishers = uiState.publishers,
                user = uiState.currentUser,
                nextEvent = uiState.nextEvent,
                nextEventPublisher = uiState.nextEventPublisher,
                nextEventParticipants = uiState.nextEventParticipants,
                onParticipateClick = viewModel::onParticipateClick
            )
        }
    }
}

@Composable
internal fun FrontpageContent(
    modifier: Modifier = Modifier,
    eventList: List<Event> = emptyList(),
    previousEvents: List<Event> = emptyList(),
    publishers: Map<String, User> = emptyMap(),
    onClickEvent: (String) -> Unit,
    onDateVotingClick: () -> Unit = {},
    onParticipateClick: (String) -> Unit,
    user: User,
    nextEvent: Event?,
    nextEventPublisher: User?,
    nextEventParticipants: List<User>
) {
    LaunchedEffect(eventList.size) {
        Log.d("FrontpageContent", "Rendering with ${eventList.size} events")
        eventList.forEach { event ->
            Log.d("FrontpageContent", "Event: ${event.eventName}, id: ${event.eventId}")
        }
    }

    val listState = rememberLazyListState()
    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                1000f // Fully collapsed when scrolled past first item
            }
        }
    }

    // Calculate scroll progress for corner radius animation (0f to 1f over 200px)
    val scrollProgress by remember {
        derivedStateOf {
            (scrollOffset / 200f).coerceIn(0f, 1f)
        }
    }

    var showAllEvents by rememberSaveable { mutableStateOf(false) }
    var showAllPreviousEvents by rememberSaveable { mutableStateOf(false) }
    val eventsToShow = if (showAllEvents) eventList else eventList.take(3)
    val previousEventsToShow = if (showAllPreviousEvents) previousEvents else previousEvents.take(1)

    val snackbarHostState = remember { SnackbarHostState() }
    var profileClickedMessage by remember { mutableStateOf("") }

    LaunchedEffect(profileClickedMessage) {
        if (profileClickedMessage.isNotEmpty()) {
            val message = profileClickedMessage
            snackbarHostState.showSnackbar(message)
            profileClickedMessage = ""
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            stickyHeader(
                contentType = "FrontPageTopBar"
            ) {
                FmAnimatableTopBar(
                    user = user,
                    scrollProgress = scrollProgress,
                    onUserClicked = {
                        profileClickedMessage = "Hov hov hov.. Profilen er ikke tilgængelig endnu 😉"
                    }
                )
            }
            item {
                FrontPageNewHeader(
                    user = user,
                    scrollProgress = scrollProgress
                )
                VSpacer(8.dp)
            }

            // Date Voting Card
            item {
                DateVotingCard(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    onClick = onDateVotingClick
                )
            }

            // Next Event section
            if (nextEvent != null) {
                val publisher = nextEventPublisher ?: User(
                    id = nextEvent.publisherId ?: "",
                    displayName = "Ukendt bruger"
                )
                item {
                    NextEventSection(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        event = nextEvent,
                        publisher = publisher,
                        participants = nextEventParticipants,
                        onParticipateClick = { onParticipateClick(nextEvent.eventId.orEmpty()) },
                        onCardClick = { onClickEvent(nextEvent.eventId.orEmpty()) },
                        onMapClick = { onClickEvent(nextEvent.eventId.orEmpty()) },
                        // TODO: Zlatan 10/01/2026 This is just horrible
                        isParticipating = nextEventParticipants.any { it.id == user.id },
                        isLoading = false,
                        isPublisher = user.id == nextEvent.publisherId
                    )
                }
            }

            // Section title for upcoming events
            if (eventList.isNotEmpty()) {
                item {
                    val seeMoreOrLess = when {
                        showAllEvents && eventList.size > 3 -> "Se mindre"
                        !showAllEvents && eventList.size > 3 -> "Se alle"
                        else -> null
                    }
                    VSpacer(20.dp)
                    SectionHeader(
                        title = "Kommende Events",
                        actionText = seeMoreOrLess,
                        onActionClick = {
                            showAllEvents = !showAllEvents
                        }
                    )
                }

                items(eventsToShow) { eventDetails ->
                    val publisher = publishers[eventDetails.publisherId]
                    EventCard(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        userName = publisher?.displayName ?: "Ukendt bruger",
                        eventName = eventDetails.eventName.orEmpty(),
                        eventDate = eventDetails.eventDate.toString(),
                        eventTime = eventDetails.eventStartTime.toString(),
                        userProfilePic = publisher?.photoUrl,
                        onClick = {
                            onClickEvent(eventDetails.eventId.orEmpty())
                        }
                    )
                }
            }

            // Previous events section
            if (previousEvents.isNotEmpty()) {
                val seeMoreOrLess = when {
                    showAllPreviousEvents && previousEvents.size > 1 -> "Se mindre"
                    !showAllPreviousEvents && previousEvents.size > 1 -> "Se alle"
                    else -> null
                }
                item {
                    VSpacer(20.dp)
                    SectionHeader(
                        title = "Tidligere Events",
                        actionText = seeMoreOrLess,
                        onActionClick = {
                            showAllPreviousEvents = !showAllPreviousEvents
                        }
                    )
                }

                items(previousEventsToShow) { eventDetails ->
                    val publisher = publishers[eventDetails.publisherId]
                    EventCard(
                        modifier = Modifier
                            .alpha(0.6f)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        userName = publisher?.displayName ?: "Ukendt bruger",
                        eventName = eventDetails.eventName.orEmpty(),
                        eventDate = eventDetails.eventDate.toString(),
                        eventTime = eventDetails.eventStartTime.toString(),
                        userProfilePic = publisher?.photoUrl,
                        onClick = {
                            onClickEvent(eventDetails.eventId.orEmpty())
                        }
                    )
                }
            }

            item {
                VSpacer(20.dp)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (actionText != null) {
            ClickableText(
                text = androidx.compose.ui.text.AnnotatedString(actionText),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                onClick = { onActionClick?.invoke() }
            )
        }
    }
}

@Composable
private fun DateVotingCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Calendar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
                Column {
                    Text(
                        text = "Stem på dato",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Vælg den bedste dato for næste event",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to voting",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@PreviewLightDark()
@Composable
private fun FrontpageContentPreview() {
    val events = previewEvents(5)
    val previousEvents = previewEvents(3)
    val mockPublishers = mapOf(
        "publisher1" to User(
            id = "publisher1",
            displayName = "John Doe",
            email = "john@example.com"
        ),
        "publisher2" to User(
            id = "publisher2",
            displayName = "Jane Smith",
            email = "jane@example.com"
        )
    )
    FlotMandTheme {
        FrontpageContent(
            modifier = Modifier,
            eventList = events,
            previousEvents = previousEvents,
            publishers = mockPublishers,
            onClickEvent = {},
            onDateVotingClick = {},
            user = User.mockUserWithCounter(1).first(),
            nextEvent = events.firstOrNull(),
            nextEventPublisher = mockPublishers[events.firstOrNull()?.publisherId],
            nextEventParticipants = emptyList(),
            onParticipateClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FrontpageEmptyStatePreview() {
    FlotMandTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Ingen events endnu",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Vær den første til at oprette et event!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FrontpageErrorStatePreview() {
    FlotMandTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "😕",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Kunne ikke hente events. Prøv igen senere.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
