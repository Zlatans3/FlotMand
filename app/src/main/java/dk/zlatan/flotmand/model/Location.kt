package dk.zlatan.flotmand.model

/**
 * Represents a geographic location with latitude and longitude.
 * This is a simple data class that can be stored in Firestore and used throughout the app.
 */
data class GeoLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    /**
     * Checks if this location has valid coordinates (not 0,0 which is default/invalid).
     */
    fun isValid(): Boolean = latitude != 0.0 || longitude != 0.0


    override fun toString(): String = "($latitude, $longitude)"
}
