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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val event: Event? = null,
    val publisher: User? = null,
    val participants: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val showParticipationBottomSheet: Boolean = false,
    val isPublisher: Boolean = false
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
    private val _isPublisher = MutableStateFlow(false)

    val uiState: StateFlow<EventDetailUiState> = combine(
        _event,
        _publisher,
        _participants,
        _isLoading,
        _showParticipationBottomSheet,
        _isPublisher
    ) { event, publisher, participants, isLoading, showBottomSheet, isPublisher ->
        EventDetailUiState(
            event = event,
            publisher = publisher,
            participants = participants,
            isLoading = isLoading,
            showParticipationBottomSheet = showBottomSheet,
            isPublisher = isPublisher
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
            setLoading(true)
            try {
                Log.d(TAG, "Load event: id=$eventId")
                val event = dinnerEventService.readDinnerEvent(eventId)
                if (event == null) {
                    Log.w(TAG, "Event not found: id=$eventId")
                    clearAll()
                    _isPublisher.value = false
                    return@launch
                }

                _event.value = event
                // Compute editing permission: current user is publisher
                _isPublisher.value = (event.publisherId != null && event.publisherId == accountService.currentUserId)

                // Publisher
                loadPublisher(event.publisherId)
                // Participants (exclude publisher)
                val participantIds = event.participantIds?.filterNot { it == event.publisherId }
                loadParticipants(participantIds)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load event: ${e.message}", e)
                clearAll()
                _isPublisher.value = false
            } finally {
                setLoading(false)
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

    private fun clearAll() {
        _event.value = null
        _publisher.value = null
        _participants.value = emptyList()
        _isPublisher.value = false
    }

    private fun setLoading(isLoading: Boolean) { _isLoading.value = isLoading }

    fun onDismissParticipantsSheet() { _showParticipationBottomSheet.value = false }
    fun showParticipants() { _showParticipationBottomSheet.value = true }

    companion object { private const val TAG = "EventDetailViewModel" }
}