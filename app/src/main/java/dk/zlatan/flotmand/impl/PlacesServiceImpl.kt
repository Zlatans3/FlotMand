package dk.zlatan.flotmand.impl

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.zlatan.flotmand.model.AddressPrediction
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.service.PlacesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementation of PlacesService using Google Places API.
 *
 * This is used for address autocomplete in Add Event and place details retrieval.
 *
 * Best practices:
 * - Session tokens for billing optimization
 * - Proper error handling
 * - Country biasing for relevant results
 * - Coroutine-based async operations
 */
@Singleton
class PlacesServiceImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : PlacesService {
        private val placesClient: PlacesClient by lazy {
            Places.createClient(context)
        }

        // Session token for autocomplete (reduces cost by grouping requests)
        private var sessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

        override suspend fun getAddressPredictions(
            query: String,
            countryCode: String?,
        ): List<AddressPrediction> {
            if (query.length < 3) {
                return emptyList() // Don't search for very short queries
            }

            return withContext(Dispatchers.IO) {
                try {
                    suspendCancellableCoroutine { continuation ->
                        val requestBuilder =
                            FindAutocompletePredictionsRequest
                                .builder()
                                .setSessionToken(sessionToken)
                                .setQuery(query)

                        // Bias results to Denmark if specified
                        if (countryCode != null) {
                            requestBuilder.setCountries(listOf(countryCode))
                        }

                        // Optional: Bias results to Copenhagen area for better relevance
                        // You can remove this or make it configurable
                        requestBuilder.setLocationBias(
                            RectangularBounds.newInstance(
                                com.google.android.gms.maps.model
                                    .LatLng(55.5, 12.3), // Southwest
                                com.google.android.gms.maps.model
                                    .LatLng(55.8, 12.8), // Northeast
                            ),
                        )

                        placesClient
                            .findAutocompletePredictions(requestBuilder.build())
                            .addOnSuccessListener { response ->
                                val predictions =
                                    response.autocompletePredictions.map { prediction ->
                                        AddressPrediction(
                                            placeId = prediction.placeId,
                                            primaryText = prediction.getPrimaryText(null).toString(),
                                            secondaryText =
                                                prediction
                                                    .getSecondaryText(null)
                                                    .toString(),
                                                fullText = prediction.getFullText(null).toString(),
                                        )
                                    }
                                continuation.resume(predictions)
                            }.addOnFailureListener { exception ->
                                Log.e(
                                    TAG,
                                    "Error fetching predictions: ${exception.message}",
                                    exception,
                                )
                                continuation.resume(emptyList())
                            }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error in getAddressPredictions: ${e.message}", e)
                    emptyList()
                }
            }
        }

        override suspend fun getPlaceDetails(placeId: String): Pair<String, GeoLocation>? =
            withContext(Dispatchers.IO) {
                try {
                    suspendCancellableCoroutine { continuation ->
                        val placeFields =
                            listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG,
                            )

                        val request =
                            FetchPlaceRequest
                                .builder(placeId, placeFields)
                                .setSessionToken(sessionToken)
                                .build()

                        placesClient
                            .fetchPlace(request)
                            .addOnSuccessListener { response ->
                                val place = response.place
                                val address = place.address ?: place.name.orEmpty()
                                val latLng = place.latLng

                                if (latLng != null) {
                                    val geoLocation = GeoLocation(latLng.latitude, latLng.longitude)

                                    // Renew session token after successful place selection
                                    sessionToken = AutocompleteSessionToken.newInstance()

                                    continuation.resume(Pair(address, geoLocation))
                                    Log.d(TAG, "Fetched place details: $address at $geoLocation")
                                } else {
                                    Log.w(TAG, "Place has no coordinates: $placeId")
                                    continuation.resume(null)
                                }
                            }.addOnFailureListener { exception ->
                                Log.e(
                                    TAG,
                                    "Error fetching place details: ${exception.message}",
                                    exception,
                                )
                                continuation.resume(null)
                            }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error in getPlaceDetails: ${e.message}", e)
                    null
                }
            }

        companion object {
            private const val TAG = "PlacesService"
        }
    }
