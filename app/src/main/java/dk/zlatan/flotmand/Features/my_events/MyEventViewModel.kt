package dk.zlatan.flotmand.Features.my_events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyEventViewModel @Inject constructor(
    private val dinnerEventService: DinnerEventService
) : ViewModel() {
    val myDinnerEvents: StateFlow<List<Event>> =
        dinnerEventService.dinnerEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}