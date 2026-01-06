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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val event: Event? = null,
    val publisher: User? = null,
    val participants: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val showParticipationBottomSheet: Boolean = false
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
    private val _isLoading = MutableStateFlow(false)
    private val _showParticipationBottomSheet = MutableStateFlow(false)

    val uiState: StateFlow<EventDetailUiState> = combine(
        _event,
        _publisher,
        _participants,
        _isLoading,
        _showParticipationBottomSheet
    ) { event, publisher, participants, isLoading, showBottomSheet ->
        EventDetailUiState(
            event = event,
            publisher = publisher,
            participants = participants,
            isLoading = isLoading,
            showParticipationBottomSheet = showBottomSheet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventDetailUiState()
    )

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading event with ID: $eventId")
                val event = dinnerEventService.readDinnerEvent(eventId)

                if (event == null) {
                    Log.w(TAG, "Event not found for ID: $eventId")
                    clearEventData()
                    return@launch
                }

                Log.d(TAG, "Event loaded: ${event.eventName}")
                Log.d(TAG, "Event publisherId: ${event.publisherId}")
                Log.d(TAG, "Event participantIds: ${event.participantIds}")
                _event.value = event

                // Load publisher separately
                loadPublisher(event.publisherId)

                // Load participants, excluding the publisher
                val participantIdsWithoutPublisher = event.participantIds?.filter { it != event.publisherId }
                loadParticipants(participantIdsWithoutPublisher)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event: ${e.message}", e)
                clearEventData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadPublisher(publisherId: String?) {
        if (publisherId.isNullOrEmpty()) {
            Log.w(TAG, "Publisher ID is null or empty")
            _publisher.value = null
            return
        }

        Log.d(TAG, "Loading publisher with ID: $publisherId")
        try {
            val users = accountService.getUsersByIds(listOf(publisherId))
            Log.d(TAG, "Fetched ${users.size} users for publisher")

            if (users.isEmpty()) {
                Log.w(TAG, "No user found for publisher ID: $publisherId")
                _publisher.value = null
            } else {
                _publisher.value = users.first()
                Log.d(TAG, "Publisher loaded: ${users.first().displayName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading publisher: ${e.message}", e)
            _publisher.value = null
        }
    }

    private suspend fun loadParticipants(participantIds: List<String>?) {
        if (participantIds.isNullOrEmpty()) {
            _participants.value = emptyList()
            return
        }

        Log.d(TAG, "Loading ${participantIds.size} participants")
        _participants.value = accountService.getUsersByIds(participantIds)
    }

    private fun clearEventData() {
        _event.value = null
        _publisher.value = null
        _participants.value = emptyList()
    }

    fun onDismissParticipantsSheet() {
        _showParticipationBottomSheet.value = false
    }

    fun showParticipants() {
        _showParticipationBottomSheet.value = true
    }

    companion object {
        private const val TAG = "EventDetailViewModel"
    }
}