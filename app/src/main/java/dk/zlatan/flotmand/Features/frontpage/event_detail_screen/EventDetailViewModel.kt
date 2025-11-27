package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import dk.zlatan.flotmand.Features.frontpage.model.Event
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
}