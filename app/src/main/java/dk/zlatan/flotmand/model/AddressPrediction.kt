package dk.zlatan.flotmand.model

/**
 * Represents an address prediction/suggestion from the Places API.
 *
 * @param[primaryText] - Main address text (e.g., "Fortuna Alle 1")
 * @param[secondaryText] - Additional details (e.g., "Frederiksberg, Denmark")
 * @param[fullText] - Complete address (e.g., "Fortuna Alle 1, Frederiksberg, Denmark")
 */
data class AddressPrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String?,
    val fullText: String
) {
    /**
     * Display text for the UI, combining primary and secondary text, but omitting the country.
     * Currently strips trailing ", Denmark" or ", Danmark" from the secondary text.
     */
    val displayText: String
        get() {
            val cleanedSecondary = secondaryText
                ?.replace(Regex(",\\s*(Denmark|Danmark)$"), "")
                ?.trim()
                ?.takeIf { it.isNotBlank() }

            return if (cleanedSecondary != null) {
                "$primaryText, $cleanedSecondary"
            } else {
                primaryText
            }
        }

    companion object {
        fun mockAddressPredictionList(count: Int): List<AddressPrediction> {
            val cities = listOf("Frederiksberg", "København", "Valby", "Aarhus", "Odense")
            val street = "Flotmand Alle"
            return List(count.coerceAtLeast(0)) { idx ->
                val n = idx + 1
                val city = cities[idx % cities.size]
                AddressPrediction(
                    placeId = "mock_place_$n",
                    primaryText = "$street $n",
                    secondaryText = "$city, Denmark",
                    fullText = "$street $n, $city, Denmark"
                )
            }
        }
    }
}
