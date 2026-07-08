package dk.zlatan.flotmand.Features.frontpage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import dk.zlatan.flotmand.BuildConfig
import dk.zlatan.flotmand.Features.frontpage.debug.DebugFlagsBottomSheet
import dk.zlatan.flotmand.Features.frontpage.debug.DebugFlagsViewModel
import dk.zlatan.flotmand.Features.frontpage.event_rotation.RotationBottomSheet
import dk.zlatan.flotmand.Features.frontpage.event_rotation.RotationImagesAndNames
import dk.zlatan.flotmand.Features.frontpage.event_rotation.RotationReminderBanner
import dk.zlatan.flotmand.Features.frontpage.event_rotation.RotationTimeline
import dk.zlatan.flotmand.Features.frontpage.event_rotation.RotationTimelineItem
import dk.zlatan.flotmand.Features.frontpage.host_rotation.HostRotationSheet
import dk.zlatan.flotmand.Features.frontpage.ui.FrontPageNewHeader
import dk.zlatan.flotmand.Features.frontpage.ui.NextEventSection
import dk.zlatan.flotmand.Features.frontpage.ui.newFmTopAppBar
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.Event.Companion.previewEvents
import dk.zlatan.flotmand.model.User
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun FrontPageRoute(
    modifier: Modifier = Modifier,
    onDinnerEventClick: (String) -> Unit,
    onDateVotingClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    viewModel: FrontPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomSheetState by viewModel.bottomSheetState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showDebugSheet by remember { mutableStateOf(false) }
    var showHostRotationSheet by remember { mutableStateOf(false) }
    var headerHeightPx by remember { mutableStateOf(1) } // Avoid division by zero
    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                headerHeightPx.toFloat() // Fully collapsed when scrolled past header
            }
        }
    }
    val scrollProgress by remember {
        derivedStateOf {
            (scrollOffset / headerHeightPx).coerceIn(0f, 1f)
        }
    }

    Scaffold(
        topBar = {
            newFmTopAppBar(
                user = uiState.currentUser,
                unreadNotificationCount = uiState.unreadNotificationCount,
                onNotificationsClick = onNotificationsClick,
                onUserClicked = {
                    val userId = uiState.currentUser.id
                    if (userId.isNotBlank()) onProfileClick(userId)
                },
                // Debug flags moved to long-press so a normal tap opens the profile.
                onUserLongClicked = {
                    if (BuildConfig.DEBUG) showDebugSheet = true
                },
                // Debug builds: tapping the header logo also opens the feature-flag sheet.
                onLogoClicked =
                    if (BuildConfig.DEBUG) {
                        { showDebugSheet = true }
                    } else {
                        null
                    },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()
        when {
            uiState.isLoading -> {
                Box(
                    modifier =
                        modifier
                            .padding(top = topPadding)
                            .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    val loadingComposition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.pot_loading),
                    )
                    LottieAnimation(
                        composition = loadingComposition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(100.dp),
                    )
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = modifier.padding(paddingValues).fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.error_emoji),
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            uiState.eventList.isEmpty() && uiState.nextEvent == null && uiState.previousEvents.isEmpty() -> {
                Box(
                    modifier = modifier.padding(paddingValues).fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.no_events_emoji),
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            text = stringResource(R.string.no_events),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.be_first_to_create_event),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            else -> {
                FrontpageContent(
                    modifier =
                        modifier
                            .padding(top = topPadding)
                            .fillMaxSize(),
                    onClickEvent = onDinnerEventClick,
                    onDateVotingClick = onDateVotingClick,
                    eventList = uiState.eventList,
                    previousEvents = uiState.previousEvents,
                    publishers = uiState.publishers,
                    user = uiState.currentUser,
                    nextEvent = uiState.nextEvent,
                    nextEventPublisher = uiState.nextEventPublisher,
                    nextEventParticipants = uiState.nextEventParticipants,
                    onParticipateClick = viewModel::onParticipateClick,
                    listState = listState,
                    scrollProgress = scrollProgress,
                    onHeaderMeasured = { headerHeightPx = it },
                    rotationTimeline = uiState.rotationTimeline,
                    showRotationBanner = uiState.showRotationBanner,
                    rotationBannerMonthLabel = uiState.rotationBannerMonthLabel,
                    onBannerCreateClick = onDateVotingClick,
                    onNormalCardClick = viewModel::onHostCardClick,
                    onVacantCardClick = viewModel::onVacantCardClick,
                    showAddSelf = !uiState.isCurrentUserInRotation,
                    onAddSelf = viewModel::onAddSelfToRotation,
                    onHostRotationClick = { showHostRotationSheet = true },
                )
                RotationBottomSheet(
                    state = bottomSheetState,
                    members = uiState.groupMembers,
                    onDismiss = viewModel::onDismissBottomSheet,
                    onGiveUpSpot = viewModel::onGiveUpSpot,
                    onShowUserPicker = viewModel::onShowUserPicker,
                    onRemoveFromRotation = viewModel::onRemoveFromRotation,
                    onAssignUser = viewModel::onAssignUserToMonth,
                    onSeeProfile = { userId ->
                        // The sheet lives in its own window and would cover the pushed screen.
                        viewModel.onDismissBottomSheet()
                        onProfileClick(userId)
                    },
                )
            }
        }
    }

    if (BuildConfig.DEBUG && showDebugSheet) {
        DebugFlagsSheetHost(onDismiss = { showDebugSheet = false })
    }

    if (showHostRotationSheet) {
        HostRotationSheet(
            onDismiss = { showHostRotationSheet = false },
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun DebugFlagsSheetHost(
    onDismiss: () -> Unit,
    viewModel: DebugFlagsViewModel = hiltViewModel(),
) {
    val flags by viewModel.flags.collectAsStateWithLifecycle()
    DebugFlagsBottomSheet(
        flags = flags,
        onToggle = viewModel::toggle,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun FrontpageContent(
    modifier: Modifier = Modifier,
    eventList: List<Event> = emptyList(),
    previousEvents: List<Event> = emptyList(),
    publishers: Map<String, User> = emptyMap(),
    onClickEvent: (String) -> Unit,
    onDateVotingClick: () -> Unit = {},
    onParticipateClick: (String) -> Unit,
    user: User,
    nextEvent: Event?,
    nextEventPublisher: User?,
    nextEventParticipants: List<User>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scrollProgress: Float,
    onHeaderMeasured: (Int) -> Unit = {},
    rotationTimeline: List<RotationTimelineItem> = emptyList(),
    showRotationBanner: Boolean = false,
    rotationBannerMonthLabel: String = "",
    onBannerCreateClick: () -> Unit = {},
    onNormalCardClick: (monthId: String, hostId: String, hostName: String) -> Unit = { _, _, _ -> },
    onVacantCardClick: (monthId: String) -> Unit = {},
    showAddSelf: Boolean = false,
    onAddSelf: () -> Unit = {},
    onHostRotationClick: () -> Unit = {},
) {
    // State for showing all events
    var showAllEvents by remember { mutableStateOf(false) }
    var showAllPreviousEvents by remember { mutableStateOf(false) }
    val eventsToShow = if (showAllEvents || eventList.size <= 3) eventList else eventList.take(3)
    val previousEventsToShow =
        if (showAllPreviousEvents || previousEvents.size <= 1) {
            previousEvents
        } else {
            previousEvents.take(
                1,
            )
        }
    val snackbarHostState = remember { SnackbarHostState() }

    // Danish date formatter
    val danishFormatter = DateTimeFormatter.ofPattern("E 'd.' d MMM", Locale("da", "DK"))

    LaunchedEffect(eventList.size) {
        Log.d("FrontpageContent", "Rendering with ${eventList.size} events")
        eventList.forEach { event ->
            Log.d("FrontpageContent", "Event: ${event.eventName}, id: ${event.eventId}")
        }
    }

    // Pass scrollProgress to FrontPageNewHeader
    Box(modifier = modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        ) {
            item {
                FrontPageNewHeader(
                    user = user,
                    scrollProgress = scrollProgress,
                    modifier =
                        Modifier.onGloballyPositioned { coordinates ->
                            onHeaderMeasured(coordinates.size.height)
                        },
                )
                VSpacer(8.dp)
            }

            // Rotation reminder banner — only rendered when visible
            item {
                RotationReminderBanner(
                    visible = showRotationBanner,
                    hostingMonthLabel = rotationBannerMonthLabel,
                    onCreateClick = onBannerCreateClick,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            // Next Event section
            item {
                if (nextEvent != null) {
                    val publisher =
                        nextEventPublisher ?: User(
                            id = nextEvent.publisherId.orEmpty(),
                            displayName = "Ukendt bruger",
                        )
                    NextEventSection(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        event = nextEvent,
                        publisher = publisher,
                        participants = nextEventParticipants,
                        onParticipateClick = { onParticipateClick(nextEvent.eventId.orEmpty()) },
                        onCardClick = { onClickEvent(nextEvent.eventId.orEmpty()) },
                        onMapClick = { onClickEvent(nextEvent.eventId.orEmpty()) },
                        // TODO: Zlatan 10/01/2026 This is just horrible
                        // should be handled in ViewModel
                        isParticipating = nextEventParticipants.any { it.id == user.id },
                        isLoading = false,
                        isPublisher = user.id == nextEvent.publisherId,
                    )
                } else {
                    VSpacer(20.dp)
                    SectionHeader(
                        title = stringResource(R.string.next_event_section_title),
                    )
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.empty),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LottieAnimation(
                            composition = composition,
                            iterations = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp),
                        )
                        Text(
                            text = stringResource(R.string.no_current_event),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Section title for upcoming events
            if (eventList.isNotEmpty()) {
                item {
                    val seeMoreOrLess =
                        when {
                            showAllEvents && eventList.size > 3 -> "Se mindre"
                            !showAllEvents && eventList.size > 3 -> "Se alle"
                            else -> null
                        }
                    VSpacer(20.dp)
                    SectionHeader(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        title = stringResource(R.string.upcoming_events),
                        actionText = seeMoreOrLess,
                        onActionClick = {
                            showAllEvents = !showAllEvents
                        },
                    )
                }

                items(eventsToShow) { eventDetails ->
                    val publisher = publishers[eventDetails.publisherId]
                    val formattedDate = eventDetails.eventDate?.format(danishFormatter)?.replaceFirstChar { it.uppercase() }.orEmpty()
                    EventCard(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                        userName = publisher?.displayName ?: "Ukendt bruger",
                        eventName = eventDetails.eventName.orEmpty(),
                        eventDate = formattedDate,
                        eventTime = eventDetails.eventStartTime?.toString().orEmpty(),
                        userProfilePic = publisher?.photoUrl,
                        onClick = {
                            onClickEvent(eventDetails.eventId.orEmpty())
                        },
                    )
                }
            }

            item {
                VSpacer(20.dp)
                SectionHeader(
                    title = stringResource(R.string.next_flotte_mand),
                    actionIcon = Icons.Filled.Settings,
                    onActionClick = onHostRotationClick,
                )
                VSpacer(20.dp)

                if (rotationTimeline.isNotEmpty()) {
                    RotationTimeline(
                        items = rotationTimeline,
                        onNormalCardClick = onNormalCardClick,
                        onVacantCardClick = onVacantCardClick,
                        showAddSelf = showAddSelf,
                        onAddSelf = onAddSelf,
                    )
                } else {
                    RotationImagesAndNames(
                        onAddSelf = onAddSelf,
                    )
                }
            }
            // Previous events section
            if (previousEvents.isNotEmpty()) {
                val seeMoreOrLess =
                    when {
                        showAllPreviousEvents && previousEvents.size > 1 -> "Se mindre"
                        !showAllPreviousEvents && previousEvents.size > 1 -> "Se alle"
                        else -> null
                    }
                item {
                    VSpacer(20.dp)
                    SectionHeader(
                        title = stringResource(R.string.previous_events),
                        actionText = seeMoreOrLess,
                        onActionClick = {
                            showAllPreviousEvents = !showAllPreviousEvents
                        },
                    )
                }

                items(previousEventsToShow) { eventDetails ->
                    val publisher = publishers[eventDetails.publisherId]
                    val formattedDate = eventDetails.eventDate?.format(danishFormatter)?.replaceFirstChar { it.uppercase() }.orEmpty()
                    EventCard(
                        modifier =
                            Modifier
                                .alpha(0.6f)
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                        userName = publisher?.displayName ?: "Ukendt bruger",
                        eventName = eventDetails.eventName.orEmpty(),
                        eventDate = formattedDate,
                        eventTime = eventDetails.eventStartTime?.toString().orEmpty(),
                        userProfilePic = publisher?.photoUrl,
                        onClick = {
                            onClickEvent(eventDetails.eventId.orEmpty())
                        },
                    )
                }
            }

            item {
                VSpacer(20.dp)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    actionText: String? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        when {
            actionIcon != null -> {
                IconButton(onClick = { onActionClick?.invoke() }) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            actionText != null -> {
                ClickableText(
                    text = AnnotatedString(actionText),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onActionClick?.invoke()
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@PreviewLightDark()
@Composable
private fun FrontpageContentPreview() {
    val events = previewEvents(5)
    val previousEvents = previewEvents(3)
    val mockPublishers =
        mapOf(
            "publisher1" to
                User(
                    id = "publisher1",
                    displayName = "John Doe",
                    email = "john@example.com",
                ),
            "publisher2" to
                User(
                    id = "publisher2",
                    displayName = "Jane Smith",
                    email = "jane@example.com",
                ),
        )
    FlotMandTheme {
        // Provide dummy listState and scrollProgress for preview
        val listState = rememberLazyListState()
        val scrollProgress = 0f
        FrontpageContent(
            modifier = Modifier,
            eventList = events,
            previousEvents = previousEvents,
            publishers = mockPublishers,
            onClickEvent = {},
            onDateVotingClick = {},
            user = User.mockUserWithCounter(1).first(),
            nextEvent = events.firstOrNull(),
            nextEventPublisher = mockPublishers[events.firstOrNull()?.publisherId],
            nextEventParticipants = emptyList(),
            onParticipateClick = {},
            listState = listState,
            scrollProgress = scrollProgress,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FrontpageEmptyStatePreview() {
    FlotMandTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.no_events_emoji),
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(
                    text = stringResource(R.string.no_events),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.be_first_to_create_event),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@PreviewLightDark()
@Composable
private fun FrontpageNoCurrentEventPreview() {
    val previousEvents = previewEvents(3)
    val mockPublishers =
        mapOf(
            "publisher1" to
                User(
                    id = "publisher1",
                    displayName = "John Doe",
                    email = "john@example.com",
                ),
        )
    FlotMandTheme {
        val listState = rememberLazyListState()
        FrontpageContent(
            modifier = Modifier,
            eventList = emptyList(),
            previousEvents = previousEvents,
            publishers = mockPublishers,
            onClickEvent = {},
            onDateVotingClick = {},
            user = User.mockUserWithCounter(1).first(),
            nextEvent = null,
            nextEventPublisher = null,
            nextEventParticipants = emptyList(),
            onParticipateClick = {},
            listState = listState,
            scrollProgress = 0f,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FrontpageErrorStatePreview() {
    FlotMandTheme {
        // Provide dummy listState and scrollProgress for preview
        val listState = rememberLazyListState()
        val scrollProgress = 0f
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.error_emoji),
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(
                    text = stringResource(R.string.could_not_fetch_events),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
