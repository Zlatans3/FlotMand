package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

data class EventDetailUiState(
    val event: Event? = null,
    val participants: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val showParticipationBottomSheet: Boolean = false
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {
    private val eventId: String? = savedStateHandle["eventId"]

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
                eventId?.let { id ->
                    val event = dinnerEventService.readDinnerEvent(id)
                    _event.value = event

                    // Load participants based on participantIds
                    event?.participantIds?.let { participantIds ->
                        val users = accountService.getUsersByIds(participantIds)
                        _participants.value = users
                    }
                }
            } catch (_: Exception) {
                // Handle error - could emit error state
                _event.value = null
                _participants.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onDismissParticipantsSheet() {
        _showParticipationBottomSheet.value = false
    }

    fun showParticipants() {
        _showParticipationBottomSheet.value = true
    }
}