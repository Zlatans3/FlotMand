package dk.zlatan.flotmand.Features.frontpage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.Features.frontpage.ui.FrontPageHeader
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.Event.Companion.previewEvents
import dk.zlatan.flotmand.model.User

@Composable
fun FrontPageRoute(
    modifier: Modifier = Modifier,
    onClickEvent: (String) -> Unit,
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
                onClickEvent = onClickEvent,
                eventList = uiState.eventList,
                publishers = uiState.publishers,
                user = null
            )
        }
    }
}

@Composable
fun FrontpageContent(
    modifier: Modifier = Modifier,
    eventList: List<Event> = emptyList(),
    publishers: Map<String, User> = emptyMap(),
    onClickEvent: (String) -> Unit,
    user: User?,
) {
    LaunchedEffect(eventList.size) {
        Log.d("FrontpageContent", "Rendering with ${eventList.size} events")
        eventList.forEach { event ->
            Log.d("FrontpageContent", "Event: ${event.eventName}, id: ${event.eventId}")
        }
    }

    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
    ) {
        item {
            FrontPageHeader()
            VSpacer(12.dp)
            if (user != null) {
                Text(
                    "Bruger: ${user.displayName} (${user.email})",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                VSpacer(8.dp)
            }
        }
        items(eventList) { eventDetails ->
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
}

@Preview(showBackground = true, showSystemUi = true)
@PreviewLightDark()
@Composable
private fun FrontpageContentPreview() {
    val events = previewEvents(5)
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
            publishers = mockPublishers,
            onClickEvent = {},
            user = null
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
