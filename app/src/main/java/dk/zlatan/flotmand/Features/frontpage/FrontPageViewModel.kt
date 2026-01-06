package dk.zlatan.flotmand.Features.frontpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FrontPageUiState(
    val eventList: List<Event> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FrontPageViewModel @Inject constructor(
    dinnerEventService: DinnerEventService
) : ViewModel() {

    val uiState: StateFlow<FrontPageUiState> = dinnerEventService.allDinnerEvents
        .map { events ->
            FrontPageUiState(
                eventList = events.sortedByDescending { it.eventDate },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FrontPageUiState(isLoading = true)
        )
}