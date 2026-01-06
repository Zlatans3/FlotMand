package dk.zlatan.flotmand.Features.my_events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun MyEventScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: MyEventViewModel = hiltViewModel(),
    onAddEventClick: () -> Unit = {}
) {
    val events by viewModel.myDinnerEvents.collectAsStateWithLifecycle()

    MyEventScreen(
        modifier = modifier,
        filteredEvents = events,
        onAddEventClick = onAddEventClick
    )
}

@Composable
internal fun MyEventScreen(
    modifier: Modifier = Modifier,
    filteredEvents: List<Event>,
    onAddEventClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        MyEventContent(
            modifier = Modifier.matchParentSize(),
            filteredEvents = filteredEvents,
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
    filteredEvents: List<Event>,
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
        items(filteredEvents) { event ->
            EventCard(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                userProfilePic = event.publisher?.photoUrl,
                userName = event.publisher?.displayName ?: "Unknown",
                eventName = event.eventName ?: "No name",
                eventDate = event.eventDate?.toString() ?: "",
                eventTime = event.eventStartTime?.toString() ?: "",
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventScreenPreview() {
    MyEventContent(
        modifier = Modifier,
        filteredEvents = Event.previewEvents(2)
    )
}