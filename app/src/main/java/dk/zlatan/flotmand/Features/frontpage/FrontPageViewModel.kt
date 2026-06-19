package dk.zlatan.flotmand.Features.frontpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import dk.zlatan.flotmand.util.feature_flags.FeatureKey
import dk.zlatan.flotmand.util.feature_flags.FeatureFlagManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    ) : ViewModel() {

        private val _bottomSheetState = MutableStateFlow<RotationBottomSheetState>(RotationBottomSheetState.Hidden)
        val bottomSheetState: StateFlow<RotationBottomSheetState> = _bottomSheetState.asStateFlow()

        // Re-fetches members only when the group document itself changes (rare).
        private val groupWithMembersFlow: Flow<Pair<Group, Map<String, User>>?> =
            rotationService.observeGroup(GROUP_ID)
                .flatMapLatest { group ->
                    if (group == null) {
                        flowOf<Pair<Group, Map<String, User>>?>(null)
                    } else {
                        flow {
                            val members = accountService.getUsersByIds(group.members).associateBy { it.id }
                            emit(group to members)
                        }
                    }
                }
                .catch { emit(null) }

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
                dinnerEventService.allDinnerEvents,
                currentUserDocFlow,
                notificationService.unreadCount,
                rotationDataWithPollsFlow,
                featureFlagManager.isEnabled(FeatureKey.SHOW_NEXT_HOST_BANNER),
            ) { events, currentUser, unreadCount, (rotationData, polls), featureFlagShowBanner ->
                val today = LocalDate.now()
                val sortedEvents = events.sortedWith(compareBy { it.eventDate ?: LocalDate.MAX })
                val upcomingEvents = sortedEvents.filter { it.eventDate == null || it.eventDate!! >= today }
                val previousEvents = sortedEvents.filter { it.eventDate != null && it.eventDate!! < today }
                val publisherIds = sortedEvents.mapNotNull { it.publisherId }.distinct()
                val nextEvent = upcomingEvents.firstOrNull { it.eventDate != null }
                val displayEvents = upcomingEvents.filterNot { it.eventId == nextEvent?.eventId }

                val (publishersMap, nextEventParticipants) = coroutineScope {
                    val publishers = async {
                        if (publisherIds.isNotEmpty()) {
                            try { fetchPublishersMap(publisherIds) } catch (_: Exception) { emptyMap() }
                        } else {
                            emptyMap<String, User>()
                        }
                    }
                    val participants = async {
                        if (!nextEvent?.participantIds.isNullOrEmpty()) {
                            try {
                                accountService.getUsersByIds(nextEvent.participantIds ?: emptyList())
                            } catch (_: Exception) { emptyList() }
                        } else {
                            emptyList<User>()
                        }
                    }
                    publishers.await() to participants.await()
                }

                val nextEventPublisher = nextEvent?.publisherId?.let { publishersMap[it] }

                val (timeline, showBanner, bannerLabel) = buildRotationState(rotationData, currentUser.id, events, polls)
                val groupMembers = rotationData.members.values.toList()
                val isCurrentUserInRotation = currentUser.id in (rotationData.group?.rotationOrder ?: emptyList())

                FrontPageUiState(
                    eventList = displayEvents,
                    previousEvents = previousEvents.sortedByDescending { it.eventDate },
                    publishers = publishersMap,
                    currentUser = currentUser,
                    nextEvent = nextEvent,
                    nextEventPublisher = nextEventPublisher,
                    nextEventParticipants = nextEventParticipants,
                    isLoading = false,
                    errorMessage = null,
                    unreadNotificationCount = unreadCount,
                    rotationTimeline = timeline,
                    showRotationBanner = showBanner || featureFlagShowBanner,
                    rotationBannerMonthLabel = bannerLabel,
                    groupMembers = groupMembers,
                    isCurrentUserInRotation = isCurrentUserInRotation,
                )
            }.catch { _ ->
                emit(
                    FrontPageUiState(
                        eventList = emptyList(),
                        previousEvents = emptyList(),
                        publishers = emptyMap(),
                        currentUser = User(),
                        isLoading = false,
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

            val timeline = (0..5).map { offset ->
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
            val nextHostId = RotationCalculator.resolveHostId(group, nextMonthId, data.overrides[nextMonthId])

            val hasEventForNextMonth = events.any { event ->
                event.publisherId == currentUserId &&
                    event.eventDate?.let { YearMonth.from(it) == nextMonth } == true
            }
            val hasOpenPoll = polls.any { it.creatorId == currentUserId && it.isOpen }

            val showBanner = nextHostId == currentUserId && !hasEventForNextMonth && !hasOpenPoll
            val bannerLabel = formatMonthLabel(nextMonthId)

            return Triple(timeline, showBanner, bannerLabel)
        }

        fun onHostCardClick(monthId: String, hostId: String, hostName: String) {
            _bottomSheetState.value = RotationBottomSheetState.HostOptions(monthId, hostId, hostName)
        }

        fun onVacantCardClick(monthId: String) {
            _bottomSheetState.value = RotationBottomSheetState.UserPicker(monthId)
        }

        fun onGiveUpSpot(monthId: String, hostId: String) {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
            viewModelScope.launch {
                try { rotationService.releaseMonth(GROUP_ID, monthId, hostId) } catch (_: Exception) {}
            }
        }

        fun onShowUserPicker(monthId: String) {
            _bottomSheetState.value = RotationBottomSheetState.UserPicker(monthId)
        }

        fun onRemoveFromRotation(hostId: String) {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
            viewModelScope.launch {
                try { rotationService.removeUserFromRotation(GROUP_ID, hostId) } catch (_: Exception) {}
            }
        }

        fun onAssignUserToMonth(monthId: String, userId: String) {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
            viewModelScope.launch {
                try { rotationService.assignMonthHost(GROUP_ID, monthId, userId) } catch (_: Exception) {}
            }
        }

        fun onAddSelfToRotation() {
            viewModelScope.launch {
                try { rotationService.addUserToRotation(GROUP_ID, accountService.currentUserId) } catch (_: Exception) {}
            }
        }

        fun onDismissBottomSheet() {
            _bottomSheetState.value = RotationBottomSheetState.Hidden
        }

        private fun formatMonthLabel(monthId: String): String =
            YearMonth.parse(monthId).month
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale("da", "DK"))
                .replaceFirstChar { it.uppercase() }

        private suspend fun fetchPublishersMap(publisherIds: List<String>): Map<String, User> =
            try {
                accountService.getUsersByIds(publisherIds).associateBy { it.id }
            } catch (_: Exception) {
                emptyMap()
            }

        fun onParticipateClick(eventId: String) {
            viewModelScope.launch {
                try {
                    val events = uiState.value.eventList + listOfNotNull(uiState.value.nextEvent)
                    val currentEvent = events.firstOrNull { it.eventId == eventId } ?: return@launch
                    val userId = accountService.currentUserId
                    val existingIds = currentEvent.participantIds ?: emptyList()
                    val updatedIds = if (existingIds.contains(userId)) {
                        existingIds.filterNot { it == userId }
                    } else {
                        existingIds + userId
                    }
                    dinnerEventService.updateDinnerEvent(currentEvent.copy(participantIds = updatedIds))
                } catch (_: Exception) {
                    // swallow; flow will refresh
                }
            }
        }

        companion object {
            private const val GROUP_ID = "flotmand"
        }
    }
