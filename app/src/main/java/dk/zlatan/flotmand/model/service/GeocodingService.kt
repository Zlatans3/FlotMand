package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.GeoLocation

// TODO: Zlatan 07/01/2026 Might not needed

/**
 * Service for converting addresses to geographic coordinates (geocoding).
 */
interface GeocodingService {
    /**
     * Converts an address string to geographic coordinates.
     *
     * @param address The address to geocode (e.g., "1600 Amphitheatre Parkway, Mountain View, CA")
     * @return GeoLocation with lat/lng coordinates, or null if geocoding fails
     */
    suspend fun getLocationFromAddress(address: String): GeoLocation?

    /**
     * Converts geographic coordinates to an address string (reverse geocoding).
     *
     * @param location The coordinates to reverse geocode
     * @return Address string, or null if reverse geocoding fails
     */
    suspend fun getAddressFromLocation(location: GeoLocation): String?
}
