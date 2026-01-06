package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.AddressMapCard
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.DetailHeader
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.ParticipantsBottomSheet
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.SectionItem
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDetailScreenRoute(
    eventId: String,
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel = hiltViewModel<EventDetailViewModel, EventDetailViewModel.Factory>(
        key = eventId,
        creationCallback = { factory ->
            factory.create(eventId)
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Henter et flot event...")
                }
            }
            uiState.event == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Event ikke fundet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Eventet kunne ikke indlæses eller eksisterer ikke",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                EventDetailScreenContent(
                    event = uiState.event!!,
                    publisher = uiState.publisher,
                    onParticipantsClick = {
                        viewModel.showParticipants()
                    },
                    onDateClick = {
                        val dateTimeString = "${
                            uiState.event!!.eventDate?.toString().orEmpty()
                        } ${uiState.event!!.eventStartTime.toString()}"
                        clipboardManager.setText(AnnotatedString(dateTimeString))
                    },
                    onLocationLongClick = {
                        val locationString = uiState.event!!.location.orEmpty()
                        clipboardManager.setText(AnnotatedString(locationString))
                    }
                )
            }
        }
    }

    if (uiState.showParticipationBottomSheet) {
        ParticipantsBottomSheet(
            onDismiss = {
                viewModel.onDismissParticipantsSheet()
            },
            participants = uiState.participants
        )
    }
}

@Composable
private fun EventDetailScreenContent(
    modifier: Modifier = Modifier,
    onParticipantsClick: () -> Unit = {},
    event: Event,
    publisher: User?,
    onDateClick: () -> Unit = {},
    onLocationLongClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use background color
    ) {
        DetailHeader(
            eventStatus = event.status,
            name = publisher?.displayName.orEmpty(),
            eventTitle = event.eventName.orEmpty(),
            // Optionally, update DetailHeader to use onSurface for text if not already
        )
        VSpacer(20.dp)
        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.Calendar,
            title = "${event.eventDate?.toString().orEmpty()} ${event.eventStartTime.toString()}",
            onClick = onDateClick,
            iconTint = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.Person,
            trailingIcon = FmIcons.chevronRight,
            title = "Deltagere: ${event.participantIds?.size ?: 0}/6",
            onClick = onParticipantsClick,
            iconTint = MaterialTheme.colorScheme.secondary,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.mapPin,
            title = event.location.orEmpty(),
            onLongClick = onLocationLongClick,
            iconTint = MaterialTheme.colorScheme.tertiary,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        VSpacer(20.dp)

        AddressMapCard(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // TODO: Zlatan 27/11/2025 Should probably do something
            },
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Deltag")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenPreview() {
    val event = Event.staticTestEvents.first()
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        publisher = User(
            id = "test-publisher",
            displayName = "Test Publisher",
            email = "publisher@test.com"
        ),
        onLocationLongClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PrintListPreview() {
    val event = Event.staticTestEvents.first()
    val publisher = User(
        id = "test-publisher",
        displayName = "Test Publisher",
        email = "publisher@test.com"
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Event: ${event.eventName}\nDate: ${event.eventDate}\nTime: ${event.eventStartTime}\nHost: ${publisher.displayName}"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenLoadingPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "Henter et flot event...")
    }
}
