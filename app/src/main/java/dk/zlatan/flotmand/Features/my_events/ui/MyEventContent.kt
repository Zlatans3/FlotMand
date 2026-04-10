package dk.zlatan.flotmand.Features.my_events.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun MyEventContent(
    eventList: List<Event>,
    modifier: Modifier = Modifier,
    publishers: Map<String, User> = emptyMap(),
    onEventClick: (String) -> Unit = {},
) {
    val danishFormatter = DateTimeFormatter.ofPattern("E 'd.' d MMM", Locale("da", "DK"))
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(eventList) { event ->
            val publisher = publishers[event.publisherId]
            val formattedDate = event.eventDate
                ?.format(danishFormatter)
                ?.replaceFirstChar { it.uppercase() }
                .orEmpty()
            EventCard(
                userProfilePic = publisher?.photoUrl,
                userName = publisher?.displayName.orEmpty(),
                eventName = event.eventName.orEmpty(),
                eventDate = formattedDate,
                eventTime = event.eventStartTime?.toString().orEmpty(),
                onClick = { event.eventId?.let { onEventClick(it) } },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventContentPreview() {
    MyEventContent(
        eventList = Event.previewEvents(3),
        publishers = mapOf(
            "Lasse" to User(
                id = "189230980f9081edwfh2109",
                displayName = "Lasse Sandø",
                email = "Sandoe@gmail.com",
            ),
        ),
    )
}
