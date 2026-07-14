package dk.zlatan.flotmand.Features.frontpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.BuildConfig
import dk.zlatan.flotmand.Features.frontpage.event_rotation.RotationBottomSheetState
import dk.zlatan.flotmand.Features.frontpage.event_rotation.RotationTimelineItem
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.Group
import dk.zlatan.flotmand.model.RotationMonth
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.NotificationService
import dk.zlatan.flotmand.model.service.RotationService
import dk.zlatan.flotmand.util.RotationCalculator
import dk.zlatan.flotmand.util.WhatsNewRepository
import dk.zlatan.flotmand.util.feature_flags.FeatureFlagManager
import dk.zlatan.flotmand.util.feature_flags.FeatureKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class FrontPageUiState(
    val eventList: List<Event> = emptyList(),
    val previousEvents: List<Event> = emptyList(),
    val publishers: Map<String, User> = emptyMap(),
    val currentUser: User,
    val nextEvent: Event? = null,
    val nextEventPublisher: User? = null,
    val nextEventParticipants: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val unreadNotificationCount: Int = 0,
    val rotationTimeline: List<RotationTimelineItem> = emptyList(),
    val showRotationBanner: Boolean = false,
    val rotationBannerMonthLabel: String = "",
    val groupMembers: List<User> = emptyList(),
    val isCurrentUserInRotation: Boolean = false,
)

private data class RotationData(
    val group: Group?,
    val members: Map<String, User>,
    val overrides: Map<String, RotationMonth>,
)

private data class EventsData(
    val events: List<Event>,
    val publishers: Map<String, User>,
    val nextEventParticipants: List<User>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FrontPageViewModel
    @Inject
    constructor(
        private val dinnerEventService: DinnerEventService,
        private val accountService: AccountService,
        private val notificationService: NotificationService,
        private val rotationService: RotationService,
        private val dateVotingService: DateVotingService,
        private val featureFlagManager: FeatureFlagManager,
        private val whatsNewRepository: WhatsNewRepository,
    ) : ViewModel() {
        private val _bottomSheetState =
            MutableStateFlow<RotationBottomSheetState>(RotationBottomSheetState.Hidden)
        val bottomSheetState: StateFlow<RotationBottomSheetState> = _bottomSheetState.asStateFlow()

        private val versionName = BuildConfig.VERSION_NAME

        val showWhatsNew: StateFlow<Boolean> =
            combine(
                whatsNewRepository.lastSeenVersion,
                featureFlagManager.isEnabled(FeatureKey.FORCE_WHATS_NEW),
            ) { lastSeenVersion, forceWhatsNew ->
                forceWhatsNew ||
                    (WhatsNewContent.entries.isNotEmpty() && lastSeenVersion != versionName)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

        fun onWhatsNewDismissed() {
            viewModelScope.launch {
                featureFlagManager.disable(FeatureKey.FORCE_WHATS_NEW)
                whatsNewRepository.markSeen(versionName)
            }
        }

        // Re-fetches members only when the group document itself changes (rare).
        private val groupWithMembersFlow: Flow<Pair<Group, Map<String, User>>?> =
            rotationService
                .observeGroup(GROUP_ID)
                .flatMapLatest { group ->
                    if (group == null) {
                        flowOf<Pair<Group, Map<String, User>>?>(null)
                    } else {
                        flow {
                            val members =
                                accountService.getUsersByIds(group.members).associateBy { it.id }
                            emit(group to members)
                        }
                    }
                }.catch { emit(null) }

        private val rotationDataFlow: Flow<RotationData> =
            combine(
                groupWithMembersFlow,
                rotationService.observeTimelineOverrides(GROUP_ID).catch { emit(emptyList()) },
            ) { groupAndMembers, overrides ->
                RotationData(
                    group = groupAndMembers?.first,
                    members = groupAndMembers?.second ?: emptyMap(),
                    overrides = overrides.associateBy { it.monthId },
                )
            }

        private val rotationDataWithPollsFlow: Flow<Pair<RotationData, List<DateVotingItem>>> =
            combine(
                rotationDataFlow,
                dateVotingService.allDateVotingsItem,
            ) { data, polls -> data to polls }

        // User lookups live in their own flows, keyed by distinctUntilChanged id lists,
        // so they only hit Firestore when the ids actually change — not every time an
        // unrelated part of the ui state (unread count, flags, rotation) emits.
        private val publishersFlow: Flow<Map<String, User>> =
            dinnerEventService.allDinnerEvents
                .map { events -> events.mapNotNull { it.publisherId }.distinct() }
                .distinctUntilChanged()
                .mapLatest { ids -> if (ids.isEmpty()) emptyMap() else fetchPublishersMap(ids) }

        private val nextEventParticipantsFlow: Flow<List<User>> =
            dinnerEventService.allDinnerEvents
                .map { events -> nextUpcomingEvent(events)?.participantIds.orEmpty() }
                .distinctUntilChanged() // only fetch participants when the list of ids changes
                .mapLatest { ids ->
                    if (ids.isEmpty()) {
                        emptyList()
                    } else {
                        try {
                            accountService.getUsersByIds(ids)
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                }

        private val eventsDataFlow: Flow<EventsData> =
            combine(
                dinnerEventService.allDinnerEvents,
                publishersFlow,
                nextEventParticipantsFlow,
            ) { events, publishers, participants ->
                EventsData(events, publishers, participants)
            }

        private val currentUserDocFlow: Flow<User> =
            accountService.currentUser
                .filterNotNull()
                .flatMapLatest { authUser ->
                    if (authUser.id.isBlank()) {
                        flowOf(authUser)
                    } else {
                        accountService.observeUserById(authUser.id).map { it ?: authUser }
                    }
                }

        val uiState: StateFlow<FrontPageUiState> =
            combine(
                eventsDataFlow,
                currentUserDocFlow,
                notificationService.unreadCount,
                rotationDataWithPollsFlow,
                featureFlagManager.isEnabled(FeatureKey.SHOW_NEXT_HOST_BANNER),
            ) { eventsData, currentUser, unreadCount, (rotationData, polls), featureFlagShowBanner ->
                val today = LocalDate.now()
                val sortedEvents =
                    eventsData.events.sortedWith(compareBy { it.eventDate ?: LocalDate.MAX })
                val (previousEvents, upcomingEvents) =
                    sortedEvents.partition { it.eventDate?.isBefore(today) == true }
                val nextEvent = nextUpcomingEvent(eventsData.events)
                val displayEvents = upcomingEvents.filterNot { it.eventId == nextEvent?.eventId }
                val nextEventPublisher = nextEvent?.publisherId?.let { eventsData.publishers[it] }

                val (timeline, showBanner, bannerLabel) =
                    buildRotationState(
                        rotationData,
                        currentUser.id,
                        eventsData.events,
                        polls,
                    )

                FrontPageUiState(
                    eventList = displayEvents,
                    previousEvents = previousEvents.sortedByDescending { it.eventDate },
                    publishers = eventsData.publishers,
                    currentUser = currentUser,
                    nextEvent = nextEvent,
                    nextEventPublisher = nextEventPublisher,
                    nextEventParticipants = eventsData.nextEventParticipants,
                    isLoading = false,
                    errorMessage = null,
                    unreadNotificationCount = unreadCount,
                    rotationTimeline = timeline,
                    showRotationBanner = showBanner || featureFlagShowBanner,
                    rotationBannerMonthLabel = bannerLabel,
                    groupMembers = rotationData.members.values.toList(),
                    isCurrentUserInRotation =
                        currentUser.id in (rotationData.group?.rotationOrder ?: emptyList()),
                )
            }.catch { throwable ->
                emit(
                    FrontPageUiState(
                        currentUser = User(),
                        isLoading = false,
                        errorMessage = throwable.message ?: GENERIC_ERROR_MESSAGE,
                    ),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FrontPageUiState(currentUser = User(), isLoading = true),
            )

        private fun buildRotationState(
            data: RotationData,
            currentUserId: String,
            events: List<Event>,
            polls: List<DateVotingItem>,
        ): Triple<List<RotationTimelineItem>, Boolean, String> {
            val group = data.group ?: return Triple(emptyList(), false, "")
            val currentMonthId = RotationCalculator.currentMonthId(group.timezone)

            val timeline =
                (0..5).map { offset ->
                    val monthId = YearMonth.parse(currentMonthId).plusMonths(offset.toLong()).toString()
                    val label = formatMonthLabel(monthId)
                    val override = data.overrides[monthId]
                    val hostId = RotationCalculator.resolveHostId(group, monthId, override)
                    val host = hostId?.let { data.members[it] }
                    if (hostId == null || host == null) {
                        RotationTimelineItem.Vacant(monthId, label, isCurrent = offset == 0)
                    } else {
                        RotationTimelineItem.Normal(
                            monthId = monthId,
                            monthLabel = label,
                            isCurrent = offset == 0,
                            hostId = hostId,
                            hostName = host.displayName,
                            hostPhotoUrl = host.photoUrl,
                        )
                    }
                }

            val nextMonthId = RotationCalculator.nextMonthId(group.timezone)
            val nextMonth = YearMonth.parse(nextMonthId)
            val nextHostId =
                RotationCalculator.resolveHostId(group, nextMonthId, data.overrides[nextMonthId])

            val hasEventForNextMonth =
                events.any { event ->
                    event.publisherId == currentUserId &&
                        event.eventDate?.let { YearMonth.from(it) == nextMonth } == true
                }
            val hasOpenPoll = polls.any { it.creatorId == currentUserId && it.isOpen }

            val showBanner = nextHostId == currentUserId && !hasEventForNextMonth && !hasOpenPoll
            val bannerLabel = formatMonthLabel(nextMonthId)

            return Triple(timeline, showBanner, bannerLabel)
        }

        fun onHostCardClick(
            monthId: String,
            hostId: String,
            hostName: String,
        ) {
            _bottomSheetState.value = RotationBottomSheetState.HostOptions(monthId, hostId, hostName)
        }

        fun onVacantCardClick(monthId: String) {
            _bottomSheetState.value = RotationBottomSheetState.UserPicker(monthId)
        }

        fun onGiveUpSpot(
            monthId: String,
            hostId: String,
        ) {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
            launchIgnoringFailure { rotationService.releaseMonth(GROUP_ID, monthId, hostId) }
        }

        fun onShowUserPicker(monthId: String) {
            _bottomSheetState.value = RotationBottomSheetState.UserPicker(monthId)
        }

        fun onRemoveFromRotation(hostId: String) {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
            launchIgnoringFailure { rotationService.removeUserFromRotation(GROUP_ID, hostId) }
        }

        fun onAssignUserToMonth(
            monthId: String,
            userId: String,
        ) {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
            launchIgnoringFailure { rotationService.assignMonthHost(GROUP_ID, monthId, userId) }
        }

        fun onAddSelfToRotation() {
            launchIgnoringFailure {
                rotationService.addUserToRotation(GROUP_ID, accountService.currentUserId)
            }
        }

        fun onDismissBottomSheet() {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
        }

        private fun formatMonthLabel(monthId: String): String =
            YearMonth
                .parse(monthId)
                .month
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale("da", "DK"))
                .replaceFirstChar { it.uppercase() }

        private suspend fun fetchPublishersMap(publisherIds: List<String>): Map<String, User> =
            try {
                accountService.getUsersByIds(publisherIds).associateBy { it.id }
            } catch (_: Exception) {
                emptyMap()
            }

        private fun nextUpcomingEvent(events: List<Event>): Event? {
            val today = LocalDate.now()
            return events
                .filter { event -> event.eventDate?.let { it >= today } == true }
                .minByOrNull { it.eventDate!! }
        }

        // Fire-and-forget writes: the Firestore listeners refresh the ui state,
        // so failures are intentionally swallowed.
        private fun launchIgnoringFailure(block: suspend () -> Unit) {
            viewModelScope.launch {
                try {
                    block()
                } catch (_: Exception) {
                }
            }
        }

        fun onParticipateClick(eventId: String) {
            launchIgnoringFailure {
                val events = uiState.value.eventList + listOfNotNull(uiState.value.nextEvent)
                val currentEvent =
                    events.firstOrNull { it.eventId == eventId } ?: return@launchIgnoringFailure
                val userId = accountService.currentUserId
                val existingIds = currentEvent.participantIds ?: emptyList()
                val updatedIds =
                    if (existingIds.contains(userId)) {
                        existingIds.filterNot { it == userId }
                    } else {
                        existingIds + userId
                    }
                dinnerEventService.updateDinnerEvent(currentEvent.copy(participantIds = updatedIds))
            }
        }

        companion object {
            private const val GROUP_ID = "flotmand"
            private const val GENERIC_ERROR_MESSAGE = "Noget gik galt. Prøv igen senere."
        }
    }
