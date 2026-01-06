package dk.zlatan.flotmand.Features.my_events.add_new_event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AddEventUiState(
    val event: Event = Event(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEventCreated: Boolean = false
)

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {

    private val _event = MutableStateFlow(Event())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isEventCreated = MutableStateFlow(false)

    val uiState: StateFlow<AddEventUiState> = combine(
        _event,
        _isLoading,
        _errorMessage,
        _isEventCreated
    ) { event: Event, isLoading: Boolean, errorMessage: String?, isEventCreated: Boolean ->
        AddEventUiState(
            event = event,
            isLoading = isLoading,
            errorMessage = errorMessage,
            isEventCreated = isEventCreated
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEventUiState()
    )

    fun onEventNameChange(name: String) {
        _event.value = _event.value.copy(eventName = name)
        _errorMessage.value = null
    }

    fun onLocationChange(location: String) {
        _event.value = _event.value.copy(location = location)
        _errorMessage.value = null
    }

    fun onEventDateChange(date: LocalDate) {
        _event.value = _event.value.copy(eventDate = date)
        _errorMessage.value = null
    }

    fun onEventTimeChange(time: LocalTime) {
        _event.value = _event.value.copy(eventStartTime = time)
        _errorMessage.value = null
    }


    fun createEvent() {
        viewModelScope.launch {
            val event = _event.value

            // Validate fields
            if (event.eventName.isNullOrBlank()) {
                _errorMessage.value = "Event navn er påkrævet"
                return@launch
            }

            if (event.location.isNullOrBlank()) {
                _errorMessage.value = "Lokation er påkrævet"
                return@launch
            }

            if (event.eventDate == null) {
                _errorMessage.value = "Dato er påkrævet"
                return@launch
            }

            if (event.eventStartTime == null) {
                _errorMessage.value = "Tidspunkt er påkrævet"
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentUser = accountService.getUserProfile()

                val newEvent = event.copy(
                    publisherId = accountService.currentUserId,
                    publisher = currentUser,
                    participants = emptyList()
                )

                dinnerEventService.createDinnerEvent(newEvent)

                _isLoading.value = false
                _isEventCreated.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Kunne ikke oprette event: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}