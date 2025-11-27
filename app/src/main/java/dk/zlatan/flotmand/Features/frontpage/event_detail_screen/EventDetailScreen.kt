package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.AddressMapCard
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.DetailHeader
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.ParticipantsBottomSheet
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.SectionItem
import dk.zlatan.flotmand.Features.frontpage.model.Event
import dk.zlatan.flotmand.Features.frontpage.model.EventStatus
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDetailScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val showParticipationBottomSheet by viewModel.showParticipationBottomSheet.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (event == null) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = "Henter et flot event...")
            }
        } else {
            EventDetailScreenContent(
                event = event!!,
                onParticipantsClick = {
                    viewModel.showParticipants()
                },
            )
        }
    }

    if (!showParticipationBottomSheet.isNullOrEmpty()) {
        ParticipantsBottomSheet(
            onDismiss = {
                viewModel.onDismissParticipantsSheet()
            },
            participants = event?.participants ?: emptyList()
        )
    }
}

@Composable
private fun EventDetailScreenContent(
    modifier: Modifier = Modifier,
    onParticipantsClick: () -> Unit = {},
    event: Event
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
    ) {
        DetailHeader(
            eventStatus = event.status ?: EventStatus.entries.first(),
            name = event.publisher?.displayName.orEmpty(),
            eventTitle = event.eventName.orEmpty(),
        )
        VSpacer(20.dp)
        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.Calendar,
            title = "${event.eventDate?.toString().orEmpty()} ${event.eventStartTime.toString()}",
            onClick = {}
        )

        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.Person,
            trailingIcon = FmIcons.chevronRight,
            title = "Deltagere: ${event.participants?.size ?: 0}/6",
            onClick = onParticipantsClick
        )

        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.mapPin,
            title = event.location.orEmpty(),
            onClick = {}
        )

        VSpacer(20.dp)

        AddressMapCard(
            modifier = Modifier
                .padding(horizontal = 20.dp),
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
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
    EventDetailScreenContent(
        modifier = Modifier,
        event = Event.staticTestEvents.first()
    )
}

@Preview(showBackground = true)
@Composable
private fun printListPreview() {
    val event = Event.staticTestEvents.first()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Event: ${event.eventName}\nDate: ${event.eventDate}\nTime: ${event.eventStartTime}\nHost: ${event.publisher?.displayName}"
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
