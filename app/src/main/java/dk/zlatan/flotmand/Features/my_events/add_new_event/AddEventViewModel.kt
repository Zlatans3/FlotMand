package dk.zlatan.flotmand.Features.my_events.add_new_event

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.AddressPrediction
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.PlacesService
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
    val isEventCreated: Boolean = false,
    val hasHandledCreation: Boolean = false,
    val addressPredictions: List<AddressPrediction> = emptyList(),
    val isLoadingPredictions: Boolean = false,
    val selectedGeoLocation: GeoLocation? = null,
    val locationTextFieldValue: TextFieldValue = TextFieldValue(),
    val votingItem: DateVotingItem? = null
)

@HiltViewModel(assistedFactory = AddEventViewModel.Factory::class)
class AddEventViewModel @AssistedInject constructor(
    private val dinnerEventService: DinnerEventService,
    private val accountService: AccountService,
    private val placesService: PlacesService,
    private val dateVotingService: DateVotingService,
    @Assisted private val votingId: String?
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(votingId: String?): AddEventViewModel
    }

    private val _event = MutableStateFlow(Event())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isEventCreated = MutableStateFlow(false)
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

    val uiState: StateFlow<AddEventUiState> = combine(
        _event,
        _isLoading,
        _errorMessage,
        _isEventCreated,
        _hasHandledCreation,
        _addressPredictions,
        _isLoadingPredictions,
        _selectedGeoLocation,
        _locationTextFieldValue,
        _votingItem
    ) { event: Event, isLoading: Boolean, errorMessage: String?, isEventCreated: Boolean,
        hasHandledCreation: Boolean, addressPredictions: List<AddressPrediction>,
        isLoadingPredictions: Boolean, selectedGeoLocation: GeoLocation?,
        locationTextFieldValue: TextFieldValue, votingItem: DateVotingItem? ->
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
            votingItem = votingItem
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

        searchJob = viewModelScope.launch {
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
    private fun stripCountrySuffix(address: String): String {
        return address
            .replace(Regex(",\\s*(Denmark|Danmark)\\s*$"), "")
            .trim()
    }

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
                    _event.value = _event.value.copy(
                        location = cleanedAddress,
                        geoLocation = geoLocation
                    )

                    // Position cursor after first comma (and space) - at start of city/postal code
                    val cursorPosition = cleanedAddress.indexOf(',').let { commaIndex ->
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
                    _locationTextFieldValue.value = TextFieldValue(
                        text = cleanedAddress,
                        selection = TextRange(cursorPosition)
                    )

                    _selectedGeoLocation.value = geoLocation
                    _addressPredictions.value = emptyList() // Clear predictions
                }
            } catch (_: Exception) {
                _errorMessage.value = "Kunne ikke hente adressedetaljer"
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
                // Ensure current user exists and is synced, but avoid unused variable
                accountService.reloadUser()

                val newEvent = event.copyWithDates(
                    publisherId = accountService.currentUserId,
                    participantIds = listOf(accountService.currentUserId),
                    geoLocation = event.geoLocation
                )

                dinnerEventService.createDinnerEvent(newEvent)

                // Delete the voting if it was created from a voting
                votingId?.let { id ->
                    try {
                        dateVotingService.deleteDateVoting(id)
                    } catch (_: Exception) {
                        // Log but don't fail the event creation if voting deletion fails
                        // The event was successfully created, which is the primary goal
                    }
                }

                _isLoading.value = false
                _isEventCreated.value = true
                _hasHandledCreation.value = false // allow UI to handle this once
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

    /**
     * Reset the state of the ViewModel for new event creation.
     */
    fun resetState() {
        _event.value = Event()
        _isLoading.value = false
        _errorMessage.value = null
        _isEventCreated.value = false
        _hasHandledCreation.value = false
        _addressPredictions.value = emptyList()
        _isLoadingPredictions.value = false
        _selectedGeoLocation.value = null
        _locationTextFieldValue.value = TextFieldValue()
        _votingItem.value = null
    }
}