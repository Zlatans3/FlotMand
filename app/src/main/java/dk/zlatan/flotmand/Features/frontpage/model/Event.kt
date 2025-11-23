package dk.zlatan.flotmand.Features.frontpage.model

data class Event(
    val eventId: String? = null,
    val user: User? = null,
    val eventDate: String? = null,
    val eventTime: String? = null
) {
    // No-arg constructor for Firebase
    constructor() : this(null, null, null, null)
}
