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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.wait

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
    val isParticipated: Boolean? = false,
    val isDeleted: Boolean = false,
)

@HiltViewModel(assistedFactory = EventDetailViewModel.Factory::class)
internal class EventDetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val eventId: String,
        private val dinnerEventService: DinnerEventService,
        private val accountService: AccountService,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(eventId: String): EventDetailViewModel
        }

        // Internal state flows
        private val _event = MutableStateFlow<Event?>(null)
        private val _publisher = MutableStateFlow<User?>(null)
        private val _participants = MutableStateFlow<List<User>>(emptyList())
        private val _isLoadingEvent = MutableStateFlow(false)
        private val _showParticipationBottomSheet = MutableStateFlow(false)
        private val _isPublisher = MutableStateFlow(false)
        private val _isParticipated = MutableStateFlow<Boolean?>(false)
        private val _isDeleted = MutableStateFlow(false)

        val uiState: StateFlow<EventDetailUiState> =
            combine(
                _event,
                _publisher,
                _participants,
                _isLoadingEvent,
                _showParticipationBottomSheet,
                _isPublisher,
                _isParticipated,
                _isDeleted,
            ) { event, publisher, participants, isLoadingEvent, showParticipationBottomSheet, isPublisher, isParticipated, isDeleted ->
                EventDetailUiState(
                    event = event,
                    publisher = publisher,
                    participants = participants,
                    isLoadingEvent = isLoadingEvent,
                    showParticipationBottomSheet = showParticipationBottomSheet,
                    isPublisher = isPublisher,
                    isParticipated = isParticipated,
                    isDeleted = isDeleted,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                EventDetailUiState(),
            )

        init {
            observeEvent()
        }

        private fun observeEvent() {
            viewModelScope.launch {
                _isLoadingEvent.value = true
                dinnerEventService.observeDinnerEvent(eventId).collectLatest { event ->
                    if (event == null) {
                        Log.w(TAG, "Event not found or deleted: id=$eventId")
                        onEventUnavailable()
                        _isLoadingEvent.value = false
                        return@collectLatest
                    }

                    val currentUserId = accountService.currentUserId
                    val isPublisher = event.publisherId != null && event.publisherId == currentUserId
                    val isParticipated = event.participantIds?.contains(currentUserId) ?: false

                    _event.value = event
                    _isPublisher.value = isPublisher
                    _isParticipated.value = isParticipated
                    _isLoadingEvent.value = false

                    // Refresh publisher and participants when relevant fields change
                    _publisher.value = loadPublisherSync(event.publisherId)
                    _participants.value = loadParticipantsSync(event.participantIds)
                }
            }
        }

        private suspend fun loadPublisherSync(publisherId: String?): User? {
            if (publisherId.isNullOrBlank()) return null
            return try {
                accountService.getUsersByIds(listOf(publisherId)).firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load publisher: ${e.message}", e)
                null
            }
        }

        private suspend fun loadParticipantsSync(participantIds: List<String>?): List<User> {
            if (participantIds.isNullOrEmpty()) return emptyList()
            return try {
                accountService.getUsersByIds(participantIds)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load participants: ${e.message}", e)
                emptyList()
            }
        }

        fun onUserParticipate() {
            viewModelScope.launch {
                val currentEvent = _event.value
                val userId = accountService.currentUserId

                if (currentEvent == null) {
                    Log.w(TAG, "onUserParticipate called but event is null")
                    return@launch
                }

                if (currentEvent.publisherId == userId) {
                    Log.i(TAG, "Publisher cannot participate; ignoring click. publisherId=$userId")
                    return@launch
                }

                try {
                    _isParticipated.value = null
                    val existingIds = currentEvent.participantIds ?: emptyList()
                    val isCurrentlyParticipating = existingIds.contains(userId)
                    val updatedIds =
                        if (isCurrentlyParticipating) {
                            existingIds.filterNot { it == userId }
                        } else {
                            existingIds + userId
                        }
                    val updatedEvent = currentEvent.copy(participantIds = updatedIds)
                    dinnerEventService.updateDinnerEvent(updatedEvent)
                    _event.value = updatedEvent
                    _participants.value = loadParticipantsSync(updatedIds)
                    _showParticipationBottomSheet.value = false
                    _isParticipated.value = !isCurrentlyParticipating
                    val action = if (isCurrentlyParticipating) "removed" else "added"
                    Log.d(TAG, "User $userId $action to participants for event ${updatedEvent.eventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to toggle participant: ${e.message}", e)
                    _isParticipated.value = currentEvent.participantIds?.contains(userId) ?: false
                }
            }
        }

        private fun clearAll() {
            _event.value = null
            _publisher.value = null
            _participants.value = emptyList()
            _isLoadingEvent.value = false
            _showParticipationBottomSheet.value = false
            _isPublisher.value = false
            _isParticipated.value = false
            _isDeleted.value = false
        }

        // Optionally, add a new function to handle event unavailable state
        fun onEventUnavailable() {
            clearAll()
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
