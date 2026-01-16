package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.AddressMapCard
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.DetailHeader
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.ParticipantsBottomSheet
import dk.zlatan.flotmand.design_system.componenets.DateTimeInfoBox
import dk.zlatan.flotmand.design_system.componenets.ParticipantsInfoBox
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmConfirmDialog
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs

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
                        // TODO: Zlatan 11/01/2026 Might need this later
//                        onDateClick = {
//                            val dateTimeString = "${
//                                event.eventDate?.toString().orEmpty()
//                            } ${event.eventStartTime.toString()}"
//                            clipboardManager.setText(AnnotatedString(dateTimeString))
//                        },
//                        onLocationLongClick = {
//                            val locationString = event.location.orEmpty()
//                            clipboardManager.setText(AnnotatedString(locationString))
//                        },
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
                        },
                        participants = uiState.participants ?: emptyList()
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
    onMapClick: () -> Unit,
    participants: List<User>
) {
    val eventOrganizerName = if (isPublisher) "Dig" else publisher?.displayName.orEmpty()
    val scrollState = rememberScrollState()
    var showFab by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState) {
        var previous = 0
        snapshotFlow { scrollState.value }
            .map { value ->
                val delta = value - previous
                previous = value
                delta
            }
            .distinctUntilChanged()
            .collectLatest { delta ->
                if (abs(delta) > 4) {
                    showFab = delta < 0
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            DetailHeader(
                eventStatus = event.status,
                name = eventOrganizerName,
                eventTitle = event.eventName.orEmpty(),
                eventDescription = event.description,
                publisherProfileImageUrl = publisher?.photoUrl,
                isPublisher = isPublisher,
                onEditClick = onEditEvent,
                onDeleteClick = onDeleteEvent,
                onBackClick = onDismiss
            )
            VSpacer(20.dp)

            // Date and Time Info Boxes
            DateTimeInfoBox(
                date = event.eventDate,
                time = event.eventStartTime,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            VSpacer(20.dp)

            // Participants Info Box
            ParticipantsInfoBox(
                participants = event.participantIds?.mapNotNull { participantId ->
                    participants.find { it.id == participantId }
                } ?: emptyList(),
                onClick = onParticipantsClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            VSpacer(20.dp)

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            VSpacer(20.dp)

            // Only show map if we have valid coordinates
            if (geoLocation != null && geoLocation.isValid()) {
                Text(
                    text = "Lokation",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                VSpacer(12.dp)
                AddressMapCard(
                    addressName = event.streetAddress,
                    cityName = event.city,
                    geoLocation = geoLocation,
                    onCardClick = onMapClick,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            VSpacer(40.dp)

            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB overlap
        }

        if (!isPublisher && event.status == EventStatus.UPCOMING) {
            val participationText = if (isParticipating == true) "deltager" else "Deltag"
            AnimatedVisibility(
                visible = showFab,
                modifier = Modifier.align(Alignment.BottomEnd),
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = participationText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    icon = {
                        AnimatedContent(
                            targetState = isParticipating == true,
                            transitionSpec = {
                                fadeIn(tween(220)) + scaleIn(tween(220)) togetherWith  fadeOut(tween(120)) + scaleOut(tween(120))
                            }
                        ) { participating ->
                            if (participating) {
                                Icon(imageVector = FmIcons.Check, contentDescription = null)
                            }
                        }
                    },
                    onClick = onParticipateClick,
                    expanded = true,
                    containerColor = if (isParticipating == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                )
            }
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
        geoLocation = GeoLocation(latitude = 55.6761, longitude = 12.5683),
        publisher = User.mockUserWithCounter(1).first(),
        participants = User.mockUserWithCounter(5),
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
        publisher = User.mockUserWithCounter(1).first(),
        participants = User.mockUserWithCounter(5),
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
