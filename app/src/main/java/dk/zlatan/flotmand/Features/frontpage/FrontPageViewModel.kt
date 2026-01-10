package dk.zlatan.flotmand.Features.frontpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FrontPageUiState(
    val eventList: List<Event> = emptyList(),
    val publishers: Map<String, User> = emptyMap(),
    val currentUser: User,
    val nextEvent: Event? = null,
    val nextEventPublisher: User? = null,
    val nextEventParticipants: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FrontPageViewModel @Inject constructor(
    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {

    // TODO: Zlatan 09/01/2026 Putting filternotnull could hide errors when currentUser is null
    val uiState: StateFlow<FrontPageUiState> = combine(
        dinnerEventService.allDinnerEvents,
        accountService.currentUser.filterNotNull()
    ) { events, currentUser ->
        val sortedEvents = events.sortedByDescending { it.eventDate }
        val publisherIds = sortedEvents.mapNotNull { it.publisherId }.distinct()

        val publishersMap = if (publisherIds.isNotEmpty()) {
            try {
                viewModelScope.async { fetchPublishersMap(publisherIds) }.await()
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        val nextEvent = sortedEvents
            .filter { it.eventDate != null }
            .minByOrNull { it.eventDate ?: LocalDate.MAX }

        val nextEventPublisher = nextEvent?.publisherId?.let { publishersMap[it] }

        val nextEventParticipants = if (!nextEvent?.participantIds.isNullOrEmpty()) {
            try {
                viewModelScope.async {
                    accountService.getUsersByIds(nextEvent?.participantIds ?: emptyList())
                }.await()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        FrontPageUiState(
            eventList = sortedEvents,
            publishers = publishersMap,
            currentUser = currentUser,
            nextEvent = nextEvent,
            nextEventPublisher = nextEventPublisher,
            nextEventParticipants = nextEventParticipants,
            isLoading = false,
            errorMessage = null
        )
    }
        .catch { _ ->
            emit(
                FrontPageUiState(
                    eventList = emptyList(),
                    publishers = emptyMap<String, User>(),
                    currentUser = User(),
                    nextEvent = null,
                    nextEventPublisher = null,
                    nextEventParticipants = emptyList(),
                    isLoading = false,
                    errorMessage = "Kunne ikke hente events. Prøv igen senere."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FrontPageUiState(
                currentUser = User(),
                nextEvent = null,
                nextEventPublisher = null,
                nextEventParticipants = emptyList(),
                isLoading = true
            )
        )

    private suspend fun fetchPublishersMap(publisherIds: List<String>): Map<String, User> {
        return try {
            accountService.getUsersByIds(publisherIds).associateBy { it.id }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun onParticipateClick(eventId: String) {
        viewModelScope.launch {
            try {
                // Fetch current event by id
                val events = uiState.value.eventList
                val currentEvent = events.firstOrNull { it.eventId == eventId }
                if (currentEvent == null) return@launch

                val userId = accountService.currentUserId
                val existingIds = currentEvent.participantIds ?: emptyList()
                val isCurrentlyParticipating = existingIds.contains(userId)

                val updatedIds = if (isCurrentlyParticipating) {
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
                        val refreshed = current.copy(
                            // Keep other fields as-is
                            nextEventParticipants = users
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