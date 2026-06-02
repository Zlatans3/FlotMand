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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.AddressMapCard
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.DetailHeader
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.EventDetailTopAppBar
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.ParticipantsBottomSheet
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.PriceCard
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.PublisherSection
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.DateTimeInfoBox
import dk.zlatan.flotmand.design_system.componenets.ParticipantsInfoBox
import dk.zlatan.flotmand.design_system.componenets.PredictiveBackScaleContainer
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmConfirmDialog
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.User
import kotlinx.coroutines.delay
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
    onEditEvent: (String) -> Unit,
    viewModel: EventDetailViewModel =
        hiltViewModel<EventDetailViewModel, EventDetailViewModel.Factory>(
            key = eventId,
            creationCallback = { factory ->
                factory.create(eventId)
            },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDismiss()
    }

    PredictiveBackScaleContainer(modifier = modifier) {
        Scaffold(
            modifier = Modifier,
            topBar = {
                EventDetailTopAppBar(
                    onBackClick = onDismiss,
                    isPublisher = uiState.isPublisher,
                    canEdit = uiState.event?.status != EventStatus.COMPLETED,
                    onDeleteClick = { showDeleteDialog = true },
                    onEditClick = { uiState.event?.eventId?.let { onEditEvent(it) } },
                )
            },
        ) { paddingValues ->
            val topBarPadding = paddingValues.calculateTopPadding()
            when {
                uiState.event != null -> {
                    val event = uiState.event
                    if (event != null) {
                        EventDetailScreenContent(
                            modifier = Modifier.padding(top = topBarPadding),
                            event = event,
                            rsvpStatus = uiState.rsvpStatus,
                            publisher = uiState.publisher,
                            geoLocation = event.geoLocation,
                            onParticipantsClick = {
                                val hasAnyone = !event.participantIds.isNullOrEmpty() ||
                                    !event.declinedIds.isNullOrEmpty()
                                if (hasAnyone) viewModel.showParticipants()
                            },
                            isPublisher = uiState.isPublisher,
                            onAccept = { viewModel.onUserRsvp(accepted = true) },
                            onDecline = { viewModel.onUserRsvp(accepted = false) },
                            onMapClick = {
                                val intent = buildDirectionsChooserIntent(
                                    latitude = event.geoLocation?.latitude,
                                    longitude = event.geoLocation?.longitude,
                                    address = event.location,
                                )
                                context.startActivity(intent)
                            },
                            participants = uiState.participants,
                            totalPriceInput = uiState.totalPriceInput,
                            pricePerPerson = uiState.pricePerPerson,
                            isSavingPrice = uiState.isSavingPrice,
                            priceError = uiState.priceError,
                            onTotalPriceChanged = viewModel::onTotalPriceChanged,
                            onSaveTotalPrice = viewModel::saveTotalPrice,
                        )
                    }
                }

                uiState.isLoadingEvent || uiState.isDeleted -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        var show by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(300)
                            show = true
                        }
                        if (show) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(text = stringResource(R.string.loading_event))
                        }
                    }
                }

                uiState.eventError != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.error_loading_event),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = uiState.eventError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                uiState.event == null && !uiState.isDeleted -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.event_not_found),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = stringResource(R.string.event_not_loaded),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (uiState.showParticipationBottomSheet) {
            ParticipantsBottomSheet(
                onDismiss = { viewModel.onDismissParticipantsSheet() },
                participants = uiState.participants,
                declinedUsers = uiState.declinedUsers,
                publisherId = uiState.publisher?.id,
            )
        }

        if (showDeleteDialog) {
            FmConfirmDialog(
                title = stringResource(R.string.delete_event_title),
                message = stringResource(R.string.delete_event_message),
                confirmText = stringResource(R.string.delete),
                onDismiss = { showDeleteDialog = false },
                onConfirmClick = {
                    showDeleteDialog = false
                    viewModel.deleteEvent(eventId)
                },
            )
        }
    }
}

@Composable
private fun EventDetailScreenContent(
    modifier: Modifier = Modifier,
    event: Event,
    geoLocation: GeoLocation?,
    rsvpStatus: RsvpStatus = RsvpStatus.NONE,
    publisher: User?,
    isPublisher: Boolean,
    onParticipantsClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onMapClick: () -> Unit,
    participants: List<User>,
    totalPriceInput: String = "",
    pricePerPerson: Double? = null,
    isSavingPrice: Boolean = false,
    priceError: String? = null,
    onTotalPriceChanged: (String) -> Unit = {},
    onSaveTotalPrice: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var showFab by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState) {
        var previous = 0
        snapshotFlow { scrollState.value }
            .map { value ->
                val delta = value - previous
                previous = value
                delta
            }.distinctUntilChanged()
            .collectLatest { delta ->
                if (abs(delta) > 4) showFab = delta < 0
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background),
        ) {
            VSpacer(20.dp)
            DetailHeader(
                eventDate = event.eventDate,
                eventTitle = event.eventName.orEmpty(),
                eventDescription = event.description,
            )
            VSpacer(20.dp)
            PublisherSection(
                publisher = publisher,
                isPublisher = isPublisher,
                modifier = Modifier,
            )
            VSpacer(20.dp)

            DateTimeInfoBox(
                date = event.eventDate,
                time = event.eventStartTime,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            VSpacer(20.dp)

            ParticipantsInfoBox(
                participants = event.participantIds?.mapNotNull { participantId ->
                    participants.find { it.id == participantId }
                } ?: emptyList(),
                declinedCount = event.declinedIds?.size ?: 0,
                onClick = onParticipantsClick,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            VSpacer(12.dp)

            PriceCard(
                isPublisher = isPublisher,
                totalPrice = event.totalPrice,
                totalPriceInput = totalPriceInput,
                pricePerPerson = pricePerPerson,
                isSavingPrice = isSavingPrice,
                priceError = priceError,
                hostPhoneNumber = publisher?.phoneNumber,
                onTotalPriceChanged = onTotalPriceChanged,
                onSave = onSaveTotalPrice,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            VSpacer(20.dp)

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

            VSpacer(20.dp)

            if (geoLocation != null && geoLocation.isValid()) {
                Text(
                    text = "Lokation",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                VSpacer(12.dp)
                AddressMapCard(
                    addressName = event.streetAddress,
                    cityName = event.city,
                    geoLocation = geoLocation,
                    eventDate = null,
                    onCardClick = onMapClick,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }

            VSpacer(40.dp)
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (!isPublisher && event.status == EventStatus.UPCOMING) {
            AnimatedVisibility(
                visible = showFab,
                modifier = Modifier.align(Alignment.BottomEnd),
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it / 2 }),
            ) {
                RsvpFab(
                    rsvpStatus = rsvpStatus,
                    onAccept = onAccept,
                    onDecline = onDecline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun RsvpFab(
    rsvpStatus: RsvpStatus,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    // Collapse when a new RSVP choice lands
    LaunchedEffect(rsvpStatus) {
        if (rsvpStatus != RsvpStatus.LOADING) expanded = false
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Two RSVP option buttons that animate up when expanded
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // "Deltager" option
                Button(
                    onClick = {
                        onAccept()
                        expanded = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                ) {
                    Icon(
                        imageVector = FmIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(text = stringResource(R.string.participating))
                }

                // "Deltager ikke" option
                Button(
                    onClick = {
                        onDecline()
                        expanded = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                ) {
                    Icon(
                        imageVector = FmIcons.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(text = stringResource(R.string.decline))
                }
            }
        }

        // Main button: round X when expanded, extended FAB otherwise
        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                (fadeIn(tween(200)) + scaleIn(tween(200))) togetherWith
                    (fadeOut(tween(120)) + scaleOut(tween(120)))
            },
        ) { isExpanded ->
            if (isExpanded) {
                FloatingActionButton(
                    onClick = { expanded = false },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.rsvp_close_content_description),
                    )
                }
            } else {
                val isLoading = rsvpStatus == RsvpStatus.LOADING
                val rsvpText = when (rsvpStatus) {
                    RsvpStatus.ACCEPTED -> stringResource(R.string.participating)
                    RsvpStatus.DECLINED -> stringResource(R.string.decline)
                    else -> stringResource(R.string.participate)
                }
                ExtendedFloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp,
                    ),
                    text = {
                        Text(
                            text = rsvpText,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    icon = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            AnimatedContent(
                                targetState = rsvpStatus,
                                transitionSpec = {
                                    fadeIn(tween(220)) + scaleIn(tween(220)) togetherWith
                                        fadeOut(tween(120)) + scaleOut(tween(120))
                                },
                            ) { status ->
                                when (status) {
                                    RsvpStatus.ACCEPTED -> Icon(imageVector = FmIcons.Check, contentDescription = null)
                                    RsvpStatus.DECLINED -> Icon(imageVector = FmIcons.Close, contentDescription = null)
                                    else -> Spacer(Modifier)
                                }
                            }
                        }
                    },
                    onClick = { if (!isLoading) expanded = true },
                    expanded = true,
                    containerColor = when (rsvpStatus) {
                        RsvpStatus.DECLINED -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.primary
                    },
                    contentColor = when (rsvpStatus) {
                        RsvpStatus.DECLINED -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onPrimary
                    },
                )
            }
        }
    }
}

private fun buildDirectionsChooserIntent(
    latitude: Double?,
    longitude: Double?,
    address: String?,
): Intent {
    val primaryUri: Uri =
        if (latitude != null && longitude != null) {
            Uri.parse("google.navigation:q=$latitude,$longitude")
        } else {
            val encoded = Uri.encode(address.orEmpty())
            Uri.parse("google.navigation:q=$encoded")
        }

    val mapIntent = Intent(Intent.ACTION_VIEW, primaryUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    val chooser = Intent.createChooser(mapIntent, "Åbn navigation med")

    val geoQuery =
        if (latitude != null && longitude != null) {
            "geo:0,0?q=$latitude,$longitude"
        } else {
            "geo:0,0?q=" + Uri.encode(address.orEmpty())
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
        isPublisher = false,
        onParticipantsClick = {},
        onAccept = {},
        onDecline = {},
        rsvpStatus = RsvpStatus.ACCEPTED,
        onMapClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenIsPublisherPreview() {
    val event = Event.staticTestEvents.first()
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        geoLocation = null,
        publisher = User.mockUserWithCounter(1).first(),
        participants = User.mockUserWithCounter(5),
        isPublisher = true,
        onParticipantsClick = {},
        onAccept = {},
        onDecline = {},
        onMapClick = {},
    )
}

@Preview(name = "Pricing — Host", showBackground = true)
@Composable
private fun EventDetailPricingHostPreview() {
    val event = Event.staticTestEvents.first().copy(totalPrice = 300.0)
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        geoLocation = null,
        publisher = User.mockUserWithCounter(1).first(),
        participants = User.mockUserWithCounter(5),
        isPublisher = true,
        totalPriceInput = "300",
        pricePerPerson = 60.0,
        onParticipantsClick = {},
        onAccept = {},
        onDecline = {},
        onMapClick = {},
    )
}

@Preview(name = "Pricing — Deltager", showBackground = true)
@Composable
private fun EventDetailPricingDeltagerPreview() {
    val event = Event.staticTestEvents.first().copy(totalPrice = 300.0)
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        geoLocation = null,
        publisher = User.mockUserWithCounter(1).first().copy(phoneNumber = "+45 12 34 56 78"),
        participants = User.mockUserWithCounter(5),
        isPublisher = false,
        totalPriceInput = "300",
        pricePerPerson = 60.0,
        rsvpStatus = RsvpStatus.ACCEPTED,
        onParticipantsClick = {},
        onAccept = {},
        onDecline = {},
        onMapClick = {},
    )
}

@Preview(name = "Pricing — Deltager (ingen værtsnummer)", showBackground = true)
@Composable
private fun EventDetailPricingDeltagerNoPhonePreview() {
    val event = Event.staticTestEvents.first().copy(totalPrice = 300.0)
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        geoLocation = null,
        publisher = User.mockUserWithCounter(1).first().copy(phoneNumber = ""),
        participants = User.mockUserWithCounter(5),
        isPublisher = false,
        totalPriceInput = "300",
        pricePerPerson = 60.0,
        rsvpStatus = RsvpStatus.ACCEPTED,
        onParticipantsClick = {},
        onAccept = {},
        onDecline = {},
        onMapClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenLoadingPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = stringResource(R.string.loading_event))
    }
}
