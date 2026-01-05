package dk.zlatan.flotmand.Features.my_events.add_new_event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AddEventUiState(
    val eventName: String = "",
    val location: String = "",
    val eventDate: LocalDate? = null,
    val eventTime: LocalTime? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEventCreated: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false
)

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState = _uiState.asStateFlow()

    fun onEventNameChange(name: String) {
        _uiState.update { it.copy(eventName = name, errorMessage = null) }
    }

    fun onLocationChange(location: String) {
        _uiState.update { it.copy(location = location, errorMessage = null) }
    }

    fun onEventDateChange(date: LocalDate) {
        _uiState.update { it.copy(eventDate = date, errorMessage = null) }
    }

    fun onEventTimeChange(time: LocalTime) {
        _uiState.update { it.copy(eventTime = time, errorMessage = null) }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showTimePicker = true) }
    }

    fun hideTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    fun createEvent() {
        viewModelScope.launch {
            val state = _uiState.value

            // Validate fields
            if (state.eventName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Event navn er påkrævet") }
                return@launch
            }

            if (state.location.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Lokation er påkrævet") }
                return@launch
            }

            if (state.eventDate == null) {
                _uiState.update { it.copy(errorMessage = "Dato er påkrævet") }
                return@launch
            }

            if (state.eventTime == null) {
                _uiState.update { it.copy(errorMessage = "Tidspunkt er påkrævet") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUser = accountService.getUserProfile()

                val newEvent = Event(
                    publisherId = accountService.currentUserId,
                    publisher = currentUser,
                    eventName = state.eventName,
                    location = state.location,
                    eventDate = state.eventDate,
                    eventStartTime = state.eventTime,
                    participants = emptyList()
                )

                dinnerEventService.createDinnerEvent(newEvent)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEventCreated = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Kunne ikke oprette event: ${e.message}"
                    )
                }
            }
        }
    }


    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}