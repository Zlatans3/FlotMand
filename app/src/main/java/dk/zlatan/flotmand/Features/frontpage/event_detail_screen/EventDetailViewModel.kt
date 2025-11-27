package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import dk.zlatan.flotmand.Features.frontpage.model.Event
import dk.zlatan.flotmand.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val eventId: String? = savedStateHandle["eventId"]
    private val _event = MutableStateFlow<Event?>(
        Event.staticTestEvents.find { it.eventId == eventId }
    )
    val event: StateFlow<Event?> = _event

    private val _showParticipationBottomSheet = MutableStateFlow<List<User>?>(null)
    val showParticipationBottomSheet: StateFlow<List<User>?> = _showParticipationBottomSheet

    fun onDismissParticipantsSheet() {
        _showParticipationBottomSheet.value = null
    }

    fun showParticipants() {
        // Always use the latest event value to get participants
        val participants = event.value?.participants ?: emptyList()
        _showParticipationBottomSheet.value = participants
    }
}