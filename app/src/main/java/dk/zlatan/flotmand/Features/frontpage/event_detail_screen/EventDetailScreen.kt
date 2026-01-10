package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.AddressMapCard
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.DetailHeader
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.ParticipantsBottomSheet
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.SectionItem
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.SectionsParticipationItem
import dk.zlatan.flotmand.design_system.componenets.buttons.FmPrimaryButton
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmConfirmDialog
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDetailScreenRoute(
    eventId: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel<EventDetailViewModel, EventDetailViewModel.Factory>(
        key = eventId,
        creationCallback = { factory ->
            factory.create(eventId)
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsState(initial = false)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Debounced loading indicator to prevent flash on fast loads
    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoadingEvent) {
        if (uiState.isLoadingEvent) {
            // Wait a short period; only show loader if still loading
            kotlinx.coroutines.delay(300)
            if (uiState.isLoadingEvent) showLoading = true
        } else {
            showLoading = false
        }
    }

    // Navigate away immediately when deleted, before content can update
    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            onDismiss()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            // Show content if event exists OR if we're in the process of deleting
            // This prevents the empty state flash during deletion
            uiState.event != null || isDeleted -> {
                if (uiState.event != null) {
                    val event = uiState.event!!
                    EventDetailScreenContent(
                        event = event,
                        isParticipating = uiState.isParticipated,
                        publisher = uiState.publisher,
                        onDismiss = onDismiss,
                        geoLocation = event.geoLocation,
                        onParticipantsClick = {
                            if (!event.participantIds.isNullOrEmpty()) {
                                viewModel.showParticipants()
                            }
                        },
                        onDateClick = {
                            val dateTimeString = "${
                                event.eventDate?.toString().orEmpty()
                            } ${event.eventStartTime.toString()}"
                            clipboardManager.setText(AnnotatedString(dateTimeString))
                        },
                        onLocationLongClick = {
                            val locationString = event.location.orEmpty()
                            clipboardManager.setText(AnnotatedString(locationString))
                        },
                        onEditEvent = {
                            // TODO: Zlatan 06/01/2026 WILL BE ADDED IN LATER PATCH
                        },
                        onDeleteEvent = { showDeleteDialog = true },
                        isPublisher = uiState.isPublisher,
                        onParticipateClick = {
                            viewModel.onUserParticipate()
                        },
                        onMapClick = {
                            val intent = buildDirectionsChooserIntent(
                                latitude = event.geoLocation?.latitude,
                                longitude = event.geoLocation?.longitude,
                                address = event.location
                            )
                            context.startActivity(intent)
                        }
                    )
                }
                // If isDeleted is true, content will navigate away before empty state renders
            }

            // Show loader if loading (or within debounce window)
            uiState.isLoadingEvent || showLoading -> {
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

            // Only show null state when not loading and event is still null and not deleted
            else -> {
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
        }
    }

    if (uiState.showParticipationBottomSheet) {
        ParticipantsBottomSheet(
            onDismiss = {
                viewModel.onDismissParticipantsSheet()
            },
            participants = uiState.participants,
            publisherId = uiState.publisher?.id
        )
    }

    if (showDeleteDialog) {
        FmConfirmDialog(
            title = "Slet event?",
            message = "Denne handling kan ikke fortrydes.",
            confirmText = "Slet",
            onDismiss = { showDeleteDialog = false },
            onConfirmClick = {
                showDeleteDialog = false
                viewModel.deleteEvent(eventId)
            }
        )
    }
}

@Composable
private fun EventDetailScreenContent(
    modifier: Modifier = Modifier,
    event: Event,
    geoLocation: GeoLocation?,
    isParticipating: Boolean?,
    publisher: User?,
    isPublisher: Boolean,
    onDismiss: () -> Unit,
    onParticipantsClick: () -> Unit,
    onParticipateClick: () -> Unit,
    onEditEvent: () -> Unit,
    onDeleteEvent: () -> Unit,
    onDateClick: () -> Unit,
    onLocationLongClick: () -> Unit,
    onMapClick: () -> Unit,
) {
    val eventOrganizerName = if (isPublisher) "Dig" else publisher?.displayName.orEmpty()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use background color
    ) {
        DetailHeader(
            eventStatus = event.status,
            name = eventOrganizerName,
            eventTitle = event.eventName.orEmpty(),
            publisherProfileImageUrl = publisher?.photoUrl,
            isPublisher = isPublisher,
            onEditClick = onEditEvent,
            onDeleteClick = onDeleteEvent,
            onBackClick = onDismiss
        )
        VSpacer(20.dp)
        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.Calendar,
            title = "${event.eventDate?.toString().orEmpty()} ${event.eventStartTime.toString()}",
            onLongClick = onDateClick,
            iconTint = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        SectionsParticipationItem(
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

        // Only show map if we have valid coordinates
        if (geoLocation != null && geoLocation.isValid()) {
            AddressMapCard(
                modifier = Modifier
                    .padding(horizontal = 20.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                geoLocation = geoLocation,
                onClick = onMapClick // Handle map click
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        val participationText = if (isParticipating == true) {
            "deltager"
        } else {
            "Deltag"
        }
        if (!isPublisher && event.status == EventStatus.UPCOMING) {
            val participationText = if (isParticipating == true) "deltager" else "Deltag"
            FmPrimaryButton(
                text = participationText,
                onClick = onParticipateClick,
                leadingIcon = if (isParticipating == true) FmIcons.Check else null,
                isLoading = (isParticipating == null),
                isAffirmed = (isParticipating == true),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

private fun buildDirectionsChooserIntent(
    latitude: Double?,
    longitude: Double?,
    address: String?
): Intent {
    // Prefer precise lat/lng if available
    val primaryUri: Uri = if (latitude != null && longitude != null) {
        // Google Maps direction URI with coordinates
        Uri.parse("google.navigation:q=$latitude,$longitude")
    } else {
        // Fall back to address text
        val encoded = Uri.encode(address ?: "")
        Uri.parse("google.navigation:q=$encoded")
    }

    val mapIntent = Intent(Intent.ACTION_VIEW, primaryUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    // Build a generic chooser to allow other providers if Google Maps isn't installed
    val chooser = Intent.createChooser(mapIntent, "Åbn navigation med")

    // Also add a generic geo: fallback without package (some map apps listen to this)
    val geoQuery = if (latitude != null && longitude != null) {
        "geo:0,0?q=$latitude,$longitude"
    } else {
        "geo:0,0?q=" + Uri.encode(address ?: "")
    }
    val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoQuery))
    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(geoIntent))

    return chooser
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenPreview() {
    val event = Event.staticTestEvents[0]
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        geoLocation = GeoLocation(latitude = 55.6761, longitude = 12.5683), // Copenhagen
        publisher = User(
            id = "test-publisher",
            displayName = "Lasse Sandø",
            email = "publisher@test.com"
        ),
        onLocationLongClick = {},
        onDateClick = {},
        onEditEvent = {},
        onDeleteEvent = {},
        isPublisher = false,
        onParticipantsClick = {},
        onParticipateClick = {},
        isParticipating = true,
        onDismiss = {},
        onMapClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenIsPublisherPreview() {
    val event = Event.staticTestEvents.first()
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        geoLocation = null, // Test without map
        publisher = User(
            id = "user1",
            displayName = "Zlatan Stadler",
            email = "publisher@test.com",
        ),
        onLocationLongClick = {},
        onDateClick = {},
        onEditEvent = {},
        onDeleteEvent = {},
        isPublisher = true,
        onParticipantsClick = {},
        onParticipateClick = {},
        isParticipating = false,
        onDismiss = {},
        onMapClick = {}
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
