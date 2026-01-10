package dk.zlatan.flotmand.Features.my_events

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User

@Composable
internal fun MyEventScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: MyEventViewModel = hiltViewModel(),
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        Log.d("MyEventScreenRoute", "UI State - isLoading: ${uiState.isLoading}, eventCount: ${uiState.eventList.size}, error: ${uiState.errorMessage}")
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
                        text = "Henter dine events...",
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
            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📅",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ingen events endnu",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Opret dit første event!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ExtendedFloatingActionButton(
                    onClick = onAddEventClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(FmIcons.Add, contentDescription = "Add Event")
                    Spacer(Modifier.width(8.dp))
                    Text("Tilføj event", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        else -> {
            MyEventScreen(
                modifier = modifier,
                eventList = uiState.eventList,
                publishers = uiState.publishers,
                onAddEventClick = onAddEventClick,
                onEventClick = onEventClick
            )
        }
    }
}

@Composable
internal fun MyEventScreen(
    modifier: Modifier = Modifier,
    eventList: List<Event>,
    publishers: Map<String, User>,
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        MyEventContent(
            modifier = Modifier.matchParentSize(),
            eventList = eventList,
            publishers = publishers,
            onEventClick = onEventClick
        )
        ExtendedFloatingActionButton(
            onClick = onAddEventClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(FmIcons.Add, contentDescription = "Add Event")
            Spacer(Modifier.width(8.dp))
            Text("Tilføj event", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun MyEventContent(
    modifier: Modifier = Modifier,
    eventList: List<Event>,
    publishers: Map<String, User> = emptyMap(),
    onEventClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        stickyHeader {
            VSpacer(12.dp)
            Text(
                "Mine arrangementer",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        items(eventList) { event ->
            val publisher = publishers[event.publisherId]
            EventCard(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                userProfilePic = publisher?.photoUrl,
                userName = publisher?.displayName ?: "Ukendt bruger",
                eventName = event.eventName ?: "Intet navn",
                eventDate = event.eventDate?.toString() ?: "",
                eventTime = event.eventStartTime?.toString() ?: "",
                onClick = {
                    event.eventId?.let { onEventClick(it) }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventScreenPreview() {
    val mockPublishers = mapOf(
        "publisher1" to User(
            id = "publisher1",
            displayName = "John Doe",
            email = "john@example.com"
        )
    )
    MyEventContent(
        modifier = Modifier,
        eventList = Event.previewEvents(2),
        publishers = mockPublishers
    )
}