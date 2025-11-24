package dk.zlatan.flotmand.Features.frontpage.model

import dk.zlatan.flotmand.model.User

data class Event(
    val eventId: String? = null,
    val user: User? = null,
    val eventName: String? = null,
    val eventDate: String? = null,
    val eventTime: String? = null
) {
    // No-arg constructor for Firebase
    constructor() : this(null, null, null, null)

    companion object {
        private val names = listOf("Zlatan Stadler", "Gustav Rasslan", "Mikkel Rahbek", "David Sandell", "Oliver Payne")
        fun previewEvents(count: Int): List<Event> {
            return List(count) { index ->
                val name = names.random()
                Event(
                    eventId = "event${index + 1}",
                    user = User(
                        displayName = name,
                    ),
                    eventName = "Middag hos $name",
                    eventDate = "2024-06-15",
                    eventTime = "18:00"
                )
            }
        }
    }
}
