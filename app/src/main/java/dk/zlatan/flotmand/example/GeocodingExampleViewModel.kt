package dk.zlatan.flotmand.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.service.GeocodingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import jakarta.inject.Inject

// TODO: Zlatan 07/01/2026 This class is might not needed, look into this later

/**
 * Example ViewModel demonstrating best practices for using the GeocodingService.
 *
 * Usage patterns:
 * 1. Inject the service via constructor
 * 2. Use coroutines for async operations
 * 3. Expose UI state via StateFlow
 * 4. Handle loading and error states
 */
@HiltViewModel
class GeocodingExampleViewModel @Inject constructor(
    private val geocodingService: GeocodingService
) : ViewModel() {

    private val _location = MutableStateFlow<GeoLocation?>(null)
    val location: StateFlow<GeoLocation?> = _location.asStateFlow()

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Converts an address string to coordinates.
     * Example: "1600 Amphitheatre Parkway, Mountain View, CA"
     */
    fun geocodeAddress(address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val location = geocodingService.getLocationFromAddress(address)
                if (location != null) {
                    _location.value = location
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Could not find location for: $address"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Converts coordinates to an address string.
     */
    fun reverseGeocode(location: GeoLocation) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val address = geocodingService.getAddressFromLocation(location)
                if (address != null) {
                    _address.value = address
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Could not find address for coordinates"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
