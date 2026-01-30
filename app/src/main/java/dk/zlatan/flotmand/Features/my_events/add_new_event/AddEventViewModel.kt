package dk.zlatan.flotmand.Features.my_events.add_new_event

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.AddressPrediction
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
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

data class AddEventUiState(
    val event: Event = Event(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEventCreated: String? = null,
    val hasHandledCreation: Boolean = false,
    val addressPredictions: List<AddressPrediction> = emptyList(),
    val isLoadingPredictions: Boolean = false,
    val selectedGeoLocation: GeoLocation? = null,
    val locationTextFieldValue: TextFieldValue = TextFieldValue(),
    val votingItem: DateVotingItem? = null,
)

@HiltViewModel(assistedFactory = AddEventViewModel.Factory::class)
class AddEventViewModel
    @AssistedInject
    constructor(
        private val dinnerEventService: DinnerEventService,
        private val accountService: AccountService,
        private val placesService: PlacesService,
        private val dateVotingService: DateVotingService,
        private val stringProvider: StringProvider,
        @Assisted("votingId") private val votingId: String?,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted("votingId") votingId: String?,
            ): AddEventViewModel
        }


        private val _event = MutableStateFlow(Event())
        private val _isLoading = MutableStateFlow(false)
        private val _errorMessage = MutableStateFlow<String?>(null)
        private val _isEventCreated = MutableStateFlow<String?>(null)
        private val _hasHandledCreation = MutableStateFlow(false)
        private val _addressPredictions = MutableStateFlow<List<AddressPrediction>>(emptyList())
        private val _isLoadingPredictions = MutableStateFlow(false)
        private val _selectedGeoLocation = MutableStateFlow<GeoLocation?>(null)
        private val _locationTextFieldValue = MutableStateFlow(TextFieldValue())
        private val _votingItem = MutableStateFlow<DateVotingItem?>(null)

        private var searchJob: Job? = null

        init {
            // Load voting item if votingId is provided
            votingId?.let { id ->
                viewModelScope.launch {
                    try {
                        val voting = dateVotingService.getDateVoting(id)
                        _votingItem.value = voting

                        // Pre-fill event name with voting name if available
                        voting?.name?.let { name ->
                            _event.value = _event.value.copy(eventName = name)
                        }

                        // Pre-fill event date with winning date if available
                        voting?.winningDate?.localDate?.let { date ->
                            _event.value = _event.value.copyWithDates(eventDate = date)
                        }
                    } catch (_: Exception) {
                        // Silently fail if voting not found
                    }
                }
            }
        }

        val uiState: StateFlow<AddEventUiState> =
            combine(
                _event,
                _isLoading,
                _errorMessage,
                _isEventCreated,
                _hasHandledCreation,
                _addressPredictions,
                _isLoadingPredictions,
                _selectedGeoLocation,
                _locationTextFieldValue,
                _votingItem,
            ) {
                event: Event,
                isLoading: Boolean,
                errorMessage: String?,
                isEventCreated: String?,
                hasHandledCreation: Boolean,
                addressPredictions: List<AddressPrediction>,
                isLoadingPredictions: Boolean,
                selectedGeoLocation: GeoLocation?,
                locationTextFieldValue: TextFieldValue,
                votingItem: DateVotingItem?,
                ->
                AddEventUiState(
                    event = event,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    isEventCreated = isEventCreated,
                    hasHandledCreation = hasHandledCreation,
                    addressPredictions = addressPredictions,
                    isLoadingPredictions = isLoadingPredictions,
                    selectedGeoLocation = selectedGeoLocation,
                    locationTextFieldValue = locationTextFieldValue,
                    votingItem = votingItem,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEventUiState())

        fun onEventNameChange(name: String) {
            _event.value = _event.value.copy(eventName = name)
            _errorMessage.value = null
        }

        fun onLocationChange(textFieldValue: TextFieldValue) {
            _locationTextFieldValue.value = textFieldValue
            _event.value = _event.value.copy(location = textFieldValue.text)
            _errorMessage.value = null

            // Trigger address predictions with debounce
            searchAddressPredictions(textFieldValue.text)
        }

        fun onEventDateChange(date: LocalDate) {
            _event.value = _event.value.copyWithDates(eventDate = date)
            _errorMessage.value = null
        }

        fun onEventTimeChange(time: LocalTime) {
            _event.value = _event.value.copyWithDates(eventStartTime = time)
            _errorMessage.value = null
        }

        fun onDescriptionChange(description: String) {
            _event.value =
                _event.value
                    .copy(eventName = _event.value.eventName, location = _event.value.location)
                    .copyWithDates(
                        eventDate = _event.value.eventDate,
                        eventStartTime = _event.value.eventStartTime,
                    ).copy(description = description)
            _errorMessage.value = null
        }

        /**
         * Search for address predictions with debounce to avoid excessive API calls.
         */
        private fun searchAddressPredictions(query: String) {
            // Cancel previous search job
            searchJob?.cancel()

            if (query.length < 3) {
                _addressPredictions.value = emptyList()
                return
            }

            searchJob =
                viewModelScope.launch {
                    // Debounce: wait 300ms before searching
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

        /**
         * Utility to remove trailing country from an address string.
         * Currently strips ", Denmark" or ", Danmark" at the end.
         */
        private fun stripCountrySuffix(address: String): String =
            address
                .replace(Regex(",\\s*(Denmark|Danmark)\\s*$"), "")
                .trim()

        /**
         * Called when user selects an address from predictions.
         */
        fun onAddressSelected(prediction: AddressPrediction) {
            viewModelScope.launch {
                _isLoadingPredictions.value = true
                try {
                    val placeDetails = placesService.getPlaceDetails(prediction.placeId)
                    if (placeDetails != null) {
                        val (fullAddress, geoLocation) = placeDetails
                        // Clean the address to avoid including country in the text field
                        val cleanedAddress = stripCountrySuffix(fullAddress)

                        // Save both the address and geoLocation to the event
                        _event.value =
                            _event.value.copy(
                                location = cleanedAddress,
                                geoLocation = geoLocation,
                            )

                        // Position cursor after first comma (and space) - at start of city/postal code
                        val cursorPosition =
                            cleanedAddress.indexOf(',').let { commaIndex ->
                                if (commaIndex >= 0) {
                                    // Skip comma and any following spaces
                                    var pos = commaIndex + 1
                                    while (pos < cleanedAddress.length && cleanedAddress[pos] == ' ') {
                                        pos++
                                    }
                                    pos
                                } else {
                                    cleanedAddress.length
                                }
                            }
                        _locationTextFieldValue.value =
                            TextFieldValue(
                                text = cleanedAddress,
                                selection = TextRange(cursorPosition),
                            )

                        _selectedGeoLocation.value = geoLocation
                        _addressPredictions.value = emptyList() // Clear predictions
                    }
                } catch (_: Exception) {
                    _errorMessage.value =
                        stringProvider.getString(R.string.error_could_not_fetch_address_details)
                } finally {
                    _isLoadingPredictions.value = false
                }
            }
        }

        /**
         * Clear address predictions (e.g., when user dismisses dropdown).
         */
        fun clearAddressPredictions() {
            _addressPredictions.value = emptyList()
        }

        private fun validateEventFields(event: Event): String? {
            if (event.eventName.isNullOrBlank()) {
                return stringProvider.getString(R.string.error_event_name_required)
            }
            if (event.location.isNullOrBlank()) {
                return stringProvider.getString(R.string.error_location_required)
            }
            if (event.geoLocation == null) {
                return stringProvider.getString(R.string.error_geolocation_required)
            }
            if (event.eventDate == null) {
                return stringProvider.getString(R.string.error_event_date_required)
            }
            if (event.eventStartTime == null) {
                return stringProvider.getString(R.string.error_event_time_required)
            }
            return null // All required fields are valid
        }

        fun createEvent() {
            val validationError = validateEventFields(_event.value)
            if (validationError != null) {
                _errorMessage.value = validationError
                return
            }
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // Create the event in Firestore and get the new document ID
                    val eventId = dinnerEventService.createDinnerEvent(_event.value)
                    // Optionally fetch the created event if you want to update local state
                    val createdEvent = dinnerEventService.readDinnerEvent(eventId)
                    if (createdEvent != null) {
                        _event.value = createdEvent
                    }
                    // If this event was created from a voting, delete the voting
                    if (votingId != null) {
                        try {
                            dateVotingService.deleteDateVoting(votingId)
                        } catch (e: Exception) {
                            // Optionally log or show a warning, but do not block event creation
                        }
                    }
                    // Pass the Firestore-generated ID to _isEventCreated
                    _isEventCreated.value = eventId
                } catch (e: Exception) {
                    _errorMessage.value = stringProvider.getString(R.string.error_could_not_create_event)
                } finally {
                    _isLoading.value = false
//                    _errorMessage.value = stringProvider.getString(
//                        R.string.error_could_not_create_event,
//                        String.valueOf(e.message)
//                    )
                }
            }
        }

        fun clearError() {
            _errorMessage.value = null
        }

        /**
         * Reset the state of the ViewModel for new event creation.
         */
        fun resetState() {
            _event.value = Event()
            _isLoading.value = false
            _errorMessage.value = null
            _isEventCreated.value = null
            _hasHandledCreation.value = false
            _addressPredictions.value = emptyList()
            _isLoadingPredictions.value = false
            _selectedGeoLocation.value = null
            _locationTextFieldValue.value = TextFieldValue()
            _votingItem.value = null
        }
    }
