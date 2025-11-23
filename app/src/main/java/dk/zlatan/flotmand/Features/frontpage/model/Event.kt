package dk.zlatan.flotmand.Features.frontpage.model

data class Event(
    val eventId: String? = null,
    val user: User? = null,
    val eventDate: String? = null,
    val eventTime: String? = null
) {
    // No-arg constructor for Firebase
    constructor() : this(null, null, null, null)

     fun previewEvent() = Event(
        eventId = "event123",
        user = User(
            uid = "user123",
            email = ""
        ),
        eventDate = "2024-06-15",
        eventTime = "18:00"
    )
}
