package dk.zlatan.flotmand.impl

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.service.GeocodingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementation of GeocodingService using Android's built-in Geocoder.
 *
 * Best practices implemented:
 * - Caching to reduce redundant API calls
 * - Proper error handling and logging
 * - Coroutine-based async operations
 * - Fallback handling for different Android versions
 */
@Singleton
class GeocodingServiceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : GeocodingService {

    private val geocoder = Geocoder(context, Locale.getDefault())

    // Simple in-memory cache to avoid redundant geocoding requests
    private val addressCache = mutableMapOf<String, GeoLocation?>()
    private val reverseCache = mutableMapOf<GeoLocation, String?>()

    override suspend fun getLocationFromAddress(address: String): GeoLocation? {
        if (address.isBlank()) {
            Log.w(TAG, "Empty address provided")
            return null
        }

        // Check cache first
        addressCache[address]?.let {
            Log.d(TAG, "Returning cached location for: $address")
            return it
        }

        return withContext(Dispatchers.IO) {
            try {
                val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use modern async API for Android 13+
                    geocodeAddressAsync(address)
                } else {
                    // Use legacy blocking API for older versions
                    geocodeAddressLegacy(address)
                }

                // Cache the result
                addressCache[address] = location

                if (location != null) {
                    Log.d(TAG, "Geocoded '$address' to $location")
                } else {
                    Log.w(TAG, "Failed to geocode address: $address")
                }

                location
            } catch (e: IOException) {
                Log.e(TAG, "Network error during geocoding: ${e.message}", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during geocoding: ${e.message}", e)
                null
            }
        }
    }

    override suspend fun getAddressFromLocation(location: GeoLocation): String? {
        if (!location.isValid()) {
            Log.w(TAG, "Invalid location provided: $location")
            return null
        }

        // Check cache first
        reverseCache[location]?.let {
            Log.d(TAG, "Returning cached address for: $location")
            return it
        }

        return withContext(Dispatchers.IO) {
            try {
                val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    reverseGeocodeAsync(location)
                } else {
                    reverseGeocodeLegacy(location)
                }

                // Cache the result
                reverseCache[location] = address

                if (address != null) {
                    Log.d(TAG, "Reverse geocoded $location to '$address'")
                } else {
                    Log.w(TAG, "Failed to reverse geocode: $location")
                }

                address
            } catch (e: IOException) {
                Log.e(TAG, "Network error during reverse geocoding: ${e.message}", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during reverse geocoding: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Modern async geocoding for Android 13+
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun geocodeAddressAsync(address: String): GeoLocation? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(address, 1) { addresses ->
                val location = addresses.firstOrNull()?.let {
                    GeoLocation(it.latitude, it.longitude)
                }
                continuation.resume(location)
            }
        }

    /**
     * Legacy blocking geocoding for older Android versions
     */
    @Suppress("DEPRECATION")
    private fun geocodeAddressLegacy(address: String): GeoLocation? {
        return try {
            val addresses = geocoder.getFromLocationName(address, 1)
            addresses?.firstOrNull()?.let {
                GeoLocation(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Legacy geocoding failed: ${e.message}", e)
            null
        }
    }

    /**
     * Modern async reverse geocoding for Android 13+
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun reverseGeocodeAsync(location: GeoLocation): String? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                val address = addresses.firstOrNull()?.formatAddress()
                continuation.resume(address)
            }
        }

    /**
     * Legacy blocking reverse geocoding for older Android versions
     */
    @Suppress("DEPRECATION")
    private fun reverseGeocodeLegacy(location: GeoLocation): String? {
        return try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.formatAddress()
        } catch (e: Exception) {
            Log.e(TAG, "Legacy reverse geocoding failed: ${e.message}", e)
            null
        }
    }

    /**
     * Formats an Address object into a readable string
     */
    private fun Address.formatAddress(): String {
        val addressParts = mutableListOf<String>()

        // Add street address
        for (i in 0..maxAddressLineIndex) {
            getAddressLine(i)?.let { addressParts.add(it) }
        }

        return addressParts.joinToString(", ")
    }

    /**
     * Clears the geocoding cache. Useful for memory management or when fresh data is needed.
     */
    fun clearCache() {
        addressCache.clear()
        reverseCache.clear()
        Log.d(TAG, "Geocoding cache cleared")
    }

    companion object {
        private const val TAG = "GeocodingService"
    }
}
