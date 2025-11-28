package dk.zlatan.flotmand.Features.my_events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.ui.FrontPageHeader
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.Event

@Composable
internal fun MyEventScreen(
    modifier: Modifier = Modifier,
    viewModel: MyEventViewModel = hiltViewModel()
) {
    val events by viewModel.myDinnerEvents.collectAsStateWithLifecycle()
    MyEventContent(
        modifier = modifier.fillMaxSize(),
        filteredEvents = events,
    )
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
        filteredEvents = Event.previewEvents(6)
    )
}