package dk.zlatan.flotmand.Features.my_events.edit_event

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.AddressPrediction
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.PlacesService
import dk.zlatan.flotmand.util.StringProvider
import dk.zlatan.flotmand.util.combine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

// UI state for EditEvent
data class EditEventUiState(
    val event: Event = Event(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEventUpdated: Boolean = false,
    val addressPredictions: List<AddressPrediction> = emptyList(),
    val isLoadingPredictions: Boolean = false,
    val selectedGeoLocation: GeoLocation? = null,
    val locationTextFieldValue: TextFieldValue = TextFieldValue(),
)

@HiltViewModel(assistedFactory = EditEventViewModel.Factory::class)
class EditEventViewModel @AssistedInject constructor(
    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService,
    private val placesService: PlacesService,
    private val stringProvider: StringProvider,
    @Assisted("eventId") private val eventId: String,
) : ViewModel() {
    companion object {
        private const val TAG = "EditEventViewModel"
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("eventId") eventId: String): EditEventViewModel
    }

    private val _event = MutableStateFlow(Event())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isEventUpdated = MutableStateFlow(false)
    private val _addressPredictions = MutableStateFlow<List<AddressPrediction>>(emptyList())
    private val _isLoadingPredictions = MutableStateFlow(false)
    private val _selectedGeoLocation = MutableStateFlow<GeoLocation?>(null)
    private val _locationTextFieldValue = MutableStateFlow(TextFieldValue())
    private var searchJob: Job? = null

    init {
        Log.d(TAG, "ViewModel initialized with eventId: $eventId")
        // Load event for editing
        viewModelScope.launch {
            try {
                val event = dinnerEventService.readDinnerEvent(eventId)
                if (event != null) {
                    Log.d(TAG, "Loaded event: $event")
                    _event.value = event
                    _locationTextFieldValue.value = TextFieldValue(event.location ?: "")
                } else {
                    Log.w(TAG, "No event found for eventId: $eventId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event: ${e.message}", e)
                _errorMessage.value = stringProvider.getString(R.string.error_could_not_load_event)
            }
        }
    }

    val uiState: StateFlow<EditEventUiState> =
        combine(
            _event,
            _isLoading,
            _errorMessage,
            _isEventUpdated,
            _addressPredictions,
            _isLoadingPredictions,
            _selectedGeoLocation,
            _locationTextFieldValue,
        ) {
            event: Event,
            isLoading: Boolean,
            errorMessage: String?,
            isEventUpdated: Boolean,
            addressPredictions: List<AddressPrediction>,
            isLoadingPredictions: Boolean,
            selectedGeoLocation: GeoLocation?,
            locationTextFieldValue: TextFieldValue,
            ->
            EditEventUiState(
                event = event,
                isLoading = isLoading,
                errorMessage = errorMessage,
                isEventUpdated = isEventUpdated,
                addressPredictions = addressPredictions,
                isLoadingPredictions = isLoadingPredictions,
                selectedGeoLocation = selectedGeoLocation,
                locationTextFieldValue = locationTextFieldValue,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditEventUiState(),
        )

    fun onEventNameChange(name: String) {
        Log.d(TAG, "onEventNameChange: $name")
        _event.value = _event.value.copy(eventName = name)
        _errorMessage.value = null
    }

    fun onLocationChange(textFieldValue: TextFieldValue) {
        Log.d(TAG, "onLocationChange: ${textFieldValue.text}")
        _locationTextFieldValue.value = textFieldValue
        _event.value = _event.value.copy(location = textFieldValue.text)
        _errorMessage.value = null
        searchAddressPredictions(textFieldValue.text)
    }

    fun onEventDateChange(date: LocalDate) {
        Log.d(TAG, "onEventDateChange: $date")
        _event.value = _event.value.copyWithDates(eventDate = date)
        _errorMessage.value = null
    }

    fun onEventTimeChange(time: LocalTime) {
        Log.d(TAG, "onEventTimeChange: $time")
        _event.value = _event.value.copyWithDates(eventStartTime = time)
        _errorMessage.value = null
    }

    fun onDescriptionChange(description: String) {
        Log.d(TAG, "onDescriptionChange: $description")
        _event.value = _event.value.copy(description = description)
        _errorMessage.value = null
    }

    private fun searchAddressPredictions(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            _addressPredictions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _isLoadingPredictions.value = true
            try {
                val predictions = placesService.getAddressPredictions(query, "DK")
                _addressPredictions.value = predictions
            } catch (_: Exception) {
                _addressPredictions.value = emptyList()
            } finally {
                _isLoadingPredictions.value = false
            }
        }
    }

    private fun stripCountrySuffix(address: String): String =
        address.replace(Regex(",\\s*(Denmark|Danmark)\\s*$"), "").trim()

    fun onAddressSelected(prediction: AddressPrediction) {
        Log.d(TAG, "onAddressSelected: $prediction")
        viewModelScope.launch {
            _isLoadingPredictions.value = true
            try {
                val placeDetails = placesService.getPlaceDetails(prediction.placeId)
                if (placeDetails != null) {
                    val (fullAddress, geoLocation) = placeDetails
                    val cleanedAddress = stripCountrySuffix(fullAddress)
                    _event.value = _event.value.copy(location = cleanedAddress, geoLocation = geoLocation)
                    val cursorPosition = cleanedAddress.indexOf(',').let { commaIndex ->
                        if (commaIndex >= 0) {
                            var pos = commaIndex + 1
                            while (pos < cleanedAddress.length && cleanedAddress[pos] == ' ') {
                                pos++
                            }
                            pos
                        } else {
                            cleanedAddress.length
                        }
                    }
                    _locationTextFieldValue.value = TextFieldValue(text = cleanedAddress, selection = TextRange(cursorPosition))
                    _selectedGeoLocation.value = geoLocation
                    _addressPredictions.value = emptyList()
                    Log.d(TAG, "Address selected and updated: $cleanedAddress, $geoLocation")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching address details: ${e.message}", e)
                _errorMessage.value = stringProvider.getString(R.string.error_could_not_fetch_address_details)
            } finally {
                _isLoadingPredictions.value = false
            }
        }
    }

    fun clearAddressPredictions() {
        Log.d(TAG, "clearAddressPredictions called")
        _addressPredictions.value = emptyList()
    }

    fun updateEvent() {
        val event = _event.value
        Log.d(TAG, "updateEvent called for event: $event")
        if (event.eventId.isNullOrBlank()) {
            Log.w(TAG, "Invalid eventId in updateEvent")
            _errorMessage.value = stringProvider.getString(R.string.error_invalid_event_id)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dinnerEventService.updateDinnerEvent(event)
                Log.d(TAG, "Event updated successfully: $event")
                _isEventUpdated.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event: ${e.message}", e)
                _errorMessage.value = stringProvider.getString(R.string.error_could_not_update_event)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetState() {
        Log.d(TAG, "resetState called")
        _event.value = Event()
        _isLoading.value = false
        _errorMessage.value = null
        _isEventUpdated.value = false
        _addressPredictions.value = emptyList()
        _isLoadingPredictions.value = false
        _selectedGeoLocation.value = null
        _locationTextFieldValue.value = TextFieldValue()
    }
}
