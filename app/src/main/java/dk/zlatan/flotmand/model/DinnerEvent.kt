package dk.zlatan.flotmand.model


import java.time.LocalDate
import java.time.LocalTime

enum class EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED
}

data class Event(
    val eventId: String? = null,
    val publisherId: String? = null,
    val status: EventStatus? = null,
    val publisher: User? = null,
    val eventName: String? = null,
    val location: String? = null,
    val participants: List<User>? = null,
    val eventDate: LocalDate? = null,
    val eventStartTime: LocalTime? = null
) {
    // No-arg constructor for Firebase
    constructor() : this(null, null, null, null)

    companion object {
        private val names = listOf("Zlatan Stadler", "Gustav Rasslan", "Mikkel Rahbek", "David Sandell", "Oliver Payne")
        fun previewEvents(count: Int): List<Event> {
            return List(count) { index ->
                val name = names.random()
                Event(
                    eventId = "event$name${index + 1}",
                    publisher = User(
                        displayName = name,
                        email = "",
                        id = "user${index + 1}",
                        isAnonymous = false,
                        provider = "preview"
                    ),
                    location = "Fortuna alle $index",
                    eventName = "Middag hos $name",
                    eventDate = LocalDate.parse("2024-06-15"),
                    eventStartTime = LocalTime.parse("18:00")
                )
            }
        }

        // Static test data for details page
        val staticTestEvents: List<Event> = listOf(
            Event(
                eventId = "event1",
                publisher = User(
                    displayName = "Zlatan Stadler",
                    email = "zlatan@example.com",
                    id = "user1",
                    isAnonymous = false,
                    provider = "test"
                ),
                eventName = "Middag hos Zlatan",
                location = "Fortuna alle 1, 2000 Frederiksberg",
                eventDate = LocalDate.parse("2024-12-01"),
                eventStartTime = LocalTime.parse("19:00"),
                participants = User.mockUserWithCounter(5),
            ),
            Event(
                eventId = "event2",
                publisher = User(
                    displayName = "Gustav Rasslan",
                    email = "gustav@example.com",
                    id = "user2",
                    isAnonymous = false,
                    provider = "test"
                ),
                eventName = "Frokost med Gustav",
                location = "Nørrebrogade 2, 2200 København N",
                eventDate = LocalDate.parse("2024-12-05"),
                eventStartTime = LocalTime.parse("12:30"),
                participants = User.mockUserWithCounter(4),
            ),
            Event(
                eventId = "event3",
                publisherId = "3",
                publisher = User(
                    displayName = "Mikkel1 Rahbek",
                    email = "mikkel@example.com",
                    id = "user3",
                    isAnonymous = false,
                    provider = "test"
                ),
                eventName = "Brunch hos Mikkel",
                location = "Østerbrogade 3, 2100 København Ø",
                eventDate = LocalDate.parse("2024-12-10"),
                eventStartTime = LocalTime.parse("10:00")
            ),
            Event(
                eventId = "event4",
                publisherId = "3",
                publisher = User(
                    displayName = "Mikkel2 Rahbek",
                    email = "mikkel@example.com",
                    id = "user3",
                    isAnonymous = false,
                    provider = "test"
                ),
                eventName = "Brunch hos Mikkel",
                location = "Amagerbrogade 4, 2300 København S",
                eventDate = LocalDate.parse("2024-12-10"),
                eventStartTime = LocalTime.parse("10:00")
            ),
            Event(
                eventId = "event5",
                publisherId = "3",
                publisher = User(
                    displayName = "Mikkel3 Rahbek",
                    email = "mikkel@example.com",
                    id = "user3",
                    isAnonymous = false,
                    provider = "test"
                ),
                eventName = "Brunch hos Mikkel",
                location = "Vesterbrogade 5, 1620 København V",
                eventDate = LocalDate.parse("2024-12-10"),
                eventStartTime = LocalTime.parse("10:00")
            )
        )

        // Helper functions for Firebase conversion
        fun dateToString(date: LocalDate?): String? = date?.toString() // yyyy-MM-dd
        fun stringToDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }
        fun timeToString(time: LocalTime?): String? = time?.toString() // HH:mm
        fun stringToTime(timeString: String?): LocalTime? = timeString?.let { LocalTime.parse(it) }
    }
}
