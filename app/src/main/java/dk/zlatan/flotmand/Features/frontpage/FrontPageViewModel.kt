package dk.zlatan.flotmand.Features.frontpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.NotificationService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
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
)

@HiltViewModel
class FrontPageViewModel
    @Inject
    constructor(
        private val dinnerEventService: DinnerEventService,
        private val accountService: AccountService,
        private val notificationService: NotificationService,
    ) : ViewModel() {

        // TODO: Zlatan 09/01/2026 Putting filternotnull could hide errors when currentUser is null
        val uiState: StateFlow<FrontPageUiState> =
            combine(
                dinnerEventService.allDinnerEvents,
                accountService.currentUser.filterNotNull(),
                notificationService.unreadCount,
            ) { events, currentUser, unreadCount ->
                val today = LocalDate.now()
                val sortedEvents =
                    events.sortedWith(
                        compareBy<Event> { it.eventDate ?: LocalDate.MAX },
                    )

                // Separate upcoming and previous events
                val upcomingEvents = sortedEvents.filter { it.eventDate == null || it.eventDate!! >= today }
                val previousEvents = sortedEvents.filter { it.eventDate != null && it.eventDate!! < today }

                val publisherIds = sortedEvents.mapNotNull { it.publisherId }.distinct()
                val nextEvent = upcomingEvents.firstOrNull { it.eventDate != null }
                val displayEvents = upcomingEvents.filterNot { it.eventId == nextEvent?.eventId }

                // Fetch publishers and participants concurrently — they are independent.
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
                )
            }.catch { _ ->
                emit(
                    FrontPageUiState(
                        eventList = emptyList(),
                        previousEvents = emptyList(),
                        publishers = emptyMap<String, User>(),
                        currentUser = User(),
                        nextEvent = null,
                        nextEventPublisher = null,
                        nextEventParticipants = emptyList(),
                        isLoading = false,
                        errorMessage = null,
                    ),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue =
                    FrontPageUiState(
                        currentUser = User(),
                        nextEvent = null,
                        nextEventPublisher = null,
                        nextEventParticipants = emptyList(),
                        isLoading = true,
                    ),
            )

        private suspend fun fetchPublishersMap(publisherIds: List<String>): Map<String, User> =
            try {
                accountService.getUsersByIds(publisherIds).associateBy { it.id }
            } catch (_: Exception) {
                emptyMap()
            }

        fun onParticipateClick(eventId: String) {
            viewModelScope.launch {
                try {
                    // Fetch current event by id
                    val events = uiState.value.eventList + listOfNotNull(uiState.value.nextEvent)
                    val currentEvent = events.firstOrNull { it.eventId == eventId }
                    if (currentEvent == null) return@launch

                    val userId = accountService.currentUserId
                    val existingIds = currentEvent.participantIds ?: emptyList()
                    val isCurrentlyParticipating = existingIds.contains(userId)

                    val updatedIds =
                        if (isCurrentlyParticipating) {
                            existingIds.filterNot { it == userId }
                        } else {
                            existingIds + userId
                        }

                    val updatedEvent = currentEvent.copy(participantIds = updatedIds)

                    // Persist the update
                    dinnerEventService.updateDinnerEvent(updatedEvent)

                    // No direct mutation of StateFlow; rely on dinnerEventService flow to emit
                    // Optionally: refresh nextEventParticipants if this is the next event
                    val next = uiState.value.nextEvent
                    if (next?.eventId == eventId) {
                        try {
                            val users = accountService.getUsersByIds(updatedIds)
                            // Re-emit UI state with updated participants for next event
                            val current = uiState.value
                            val refreshed =
                                current.copy(
                                    // Keep other fields as-is
                                    nextEventParticipants = users,
                                )
                            // Since uiState is a StateFlow derived from combine, we cannot set it directly.
                            // The source flows will update soon via dinnerEventService; so we skip manual emit.
                        } catch (_: Exception) {
                            // ignore; flow will refresh
                        }
                    }
                } catch (_: Exception) {
                    // swallow errors here; production would report
                }
            }
        }
    }
