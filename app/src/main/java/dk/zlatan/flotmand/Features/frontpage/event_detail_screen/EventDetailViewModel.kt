package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.util.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

/**
 * UI state for Event Detail screen.
 * - isParticipated tri-state: false = not participating (default), null = loading, true = participating
 */
data class EventDetailUiState(
    val event: Event? = null,
    val publisher: User? = null,
    val participants: List<User> = emptyList(),
    val isLoadingEvent: Boolean = false,
    val showParticipationBottomSheet: Boolean = false,
    val isPublisher: Boolean = false,
    val isParticipated: Boolean? = false
)

@HiltViewModel(assistedFactory = EventDetailViewModel.Factory::class)
internal class EventDetailViewModel @AssistedInject constructor(
    @Assisted private val eventId: String,
    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(eventId: String): EventDetailViewModel
    }

    private val _event = MutableStateFlow<Event?>(null)
    private val _publisher = MutableStateFlow<User?>(null)
    private val _participants = MutableStateFlow<List<User>>(emptyList())
    private val _isLoadingEvent = MutableStateFlow(false)
    private val _showParticipationBottomSheet = MutableStateFlow(false)
    private val _isPublisher = MutableStateFlow(false)
    private val _isDeleted = MutableStateFlow(false)
    private val _isParticipated = MutableStateFlow<Boolean?>(false)

    val uiState: StateFlow<EventDetailUiState> = combine(
        _event,
        _publisher,
        _participants,
        _isLoadingEvent,
        _showParticipationBottomSheet,
        _isPublisher,
        _isParticipated
    ) { event, publisher, participants, isLoadingEvent, showBottomSheet, isPublisher, isParticipated ->
        EventDetailUiState(
            event = event,
            publisher = publisher,
            participants = participants,
            isLoadingEvent = isLoadingEvent,
            showParticipationBottomSheet = showBottomSheet,
            isPublisher = isPublisher,
            isParticipated = isParticipated
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventDetailUiState()
    )

    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    init {
        observeEvent()
    }

    private fun observeEvent() {
        viewModelScope.launch {
            setLoadingEvent(true)
            dinnerEventService.observeDinnerEvent(eventId).collectLatest { event ->
                if (event == null) {
                    Log.w(TAG, "Event not found or deleted: id=$eventId")
                    clearAll()
                    _isPublisher.value = false
                    setLoadingEvent(false)
                    return@collectLatest
                }

                _event.value = event

                val currentUserId = accountService.currentUserId
                _isPublisher.value = (event.publisherId != null && event.publisherId == currentUserId)
                _isParticipated.value = event.participantIds?.contains(currentUserId) ?: false

                // Refresh publisher and participants when relevant fields change
                launch { loadPublisher(event.publisherId) }
                launch { loadParticipants(event.participantIds) }

                setLoadingEvent(false)
            }
        }
    }

    private suspend fun loadPublisher(publisherId: String?) {
        if (publisherId.isNullOrBlank()) {
            _publisher.value = null
            return
        }
        try {
            val users = accountService.getUsersByIds(listOf(publisherId))
            _publisher.value = users.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load publisher: ${e.message}", e)
            _publisher.value = null
        }
    }

    private suspend fun loadParticipants(participantIds: List<String>?) {
        if (participantIds.isNullOrEmpty()) {
            _participants.value = emptyList()
            return
        }
        try {
            _participants.value = accountService.getUsersByIds(participantIds)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load participants: ${e.message}", e)
            _participants.value = emptyList()
        }
    }

    /**
     * Logic might change to reflect a seperate button for "Join" vs "Leave" in future.
     */
    fun onUserParticipate() {
        // Toggle current user's participation in the event and persist the change
        viewModelScope.launch {
            val currentEvent = _event.value
            val userId = accountService.currentUserId

            if (currentEvent == null) {
                Log.w(TAG, "onUserParticipate called but event is null")
                return@launch
            }

            // Guard: publisher should not be able to participate via this button
            if (currentEvent.publisherId == userId) {
                Log.i(TAG, "Publisher cannot participate; ignoring click. publisherId=$userId")
                return@launch
            }

            try {
                // Set to null to indicate loading state
                _isParticipated.value = null

                // Start from current event participantIds (default to empty)
                val existingIds = currentEvent.participantIds ?: emptyList()
                val isCurrentlyParticipating = existingIds.contains(userId)

                // Toggle: remove if present, add if missing
                val updatedIds = if (isCurrentlyParticipating) {
                    existingIds.filterNot { it == userId }
                } else {
                    existingIds + userId
                }

                val updatedEvent = currentEvent.copy(participantIds = updatedIds)

                // Persist the update
                dinnerEventService.updateDinnerEvent(updatedEvent)

                // Update local state
                _event.value = updatedEvent

                // Reload participants for UI (including publisher/host)
                loadParticipants(updatedIds)

                // Optionally dismiss the sheet after successful toggle
                _showParticipationBottomSheet.value = false
                // Set final participation state (true if added, false if removed)
                _isParticipated.value = !isCurrentlyParticipating

                val action = if (isCurrentlyParticipating) "removed" else "added"
                Log.d(TAG, "User $userId $action to participants for event ${updatedEvent.eventId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle participant: ${e.message}", e)
                // Revert to previous state on error
                _isParticipated.value = currentEvent.participantIds?.contains(userId) ?: false
            }
        }
    }

    private fun clearAll() {
        _event.value = null
        _publisher.value = null
        _participants.value = emptyList()
        _isPublisher.value = false
        _isParticipated.value = false
    }

    private fun setLoadingEvent(isLoading: Boolean) {
        _isLoadingEvent.value = isLoading
    }

    fun onDismissParticipantsSheet() {
        _showParticipationBottomSheet.value = false
    }

    fun showParticipants() {
        _showParticipationBottomSheet.value = true
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                dinnerEventService.deleteDinnerEvent(eventId)
                // Clear UI state after deletion
                clearAll()
                _isDeleted.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete event: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "EventDetailViewModel"
    }
}