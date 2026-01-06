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
    private val _participants = MutableStateFlow<List<User>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _showParticipationBottomSheet = MutableStateFlow(false)

    val uiState: StateFlow<EventDetailUiState> = combine(
        _event,
        _participants,
        _isLoading,
        _showParticipationBottomSheet
    ) { event, participants, isLoading, showBottomSheet ->
        EventDetailUiState(
            event = event,
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
                    _event.value = null
                    _participants.value = emptyList()
                } else {
                    Log.d(TAG, "Event loaded successfully: ${event.eventName}")
                    _event.value = event

                    // Load participants based on participantIds
                    event.participantIds?.let { participantIds ->
                        if (participantIds.isNotEmpty()) {
                            Log.d(TAG, "Loading ${participantIds.size} participants")
                            val users = accountService.getUsersByIds(participantIds)
                            _participants.value = users
                            Log.d(TAG, "Loaded ${users.size} participants")
                        } else {
                            Log.d(TAG, "No participants for this event")
                            _participants.value = emptyList()
                        }
                    } ?: run {
                        Log.d(TAG, "participantIds is null")
                        _participants.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event: ${e.message}", e)
                _event.value = null
                _participants.value = emptyList()
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Loading complete. isLoading set to false")
            }
        }
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