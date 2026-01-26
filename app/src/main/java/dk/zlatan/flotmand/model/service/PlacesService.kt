package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.AddressPrediction
import dk.zlatan.flotmand.model.GeoLocation

/**
 * Service for address autocomplete and place details lookup.
 */
interface PlacesService {
    suspend fun getAddressPredictions(
        query: String,
        countryCode: String? = "DK",
    ): List<AddressPrediction>

    suspend fun getPlaceDetails(placeId: String): Pair<String, GeoLocation>?
}
