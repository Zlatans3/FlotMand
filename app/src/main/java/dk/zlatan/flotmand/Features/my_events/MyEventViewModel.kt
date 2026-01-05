package dk.zlatan.flotmand.Features.my_events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.Event.Companion.staticTestEvents
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyEventViewModel @Inject constructor(
    // in a perfect world
//    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService // Inject AccountService
) : ViewModel() {
    val currentUserId: String
        get() = "3"
            //accountService.currentUserId

    val myDinnerEvents: StateFlow<List<Event>> =
        MutableStateFlow(
            staticTestEvents.filter { it.publisherId == currentUserId }
        )
}