package dk.zlatan.flotmand.model


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED
}

data class Event(
    @DocumentId
    val eventId: String? = null,
    val publisherId: String? = null,
    val participantIds: List<String>? = null,
    val eventName: String? = null,
    val location: String? = null,
    // Firestore-compatible fields (stored as Strings)
    var eventDateString: String? = null,
    var eventStartTimeString: String? = null
) {
    // Computed properties for LocalDate and LocalTime (excluded from Firestore)
    @get:Exclude
    var eventDate: LocalDate?
        get() = eventDateString?.let { LocalDate.parse(it) }
        set(value) {
            eventDateString = value?.toString()
        }

    @get:Exclude
    var eventStartTime: LocalTime?
        get() = eventStartTimeString?.let { LocalTime.parse(it) }
        set(value) {
            eventStartTimeString = value?.toString()
        }

    // Helper function to copy with proper handling of dates/times
    fun copyWithDates(
        eventId: String? = this.eventId,
        publisherId: String? = this.publisherId,
        participantIds: List<String>? = this.participantIds,
        eventName: String? = this.eventName,
        location: String? = this.location,
        eventDate: LocalDate? = this.eventDate,
        eventStartTime: LocalTime? = this.eventStartTime
    ): Event {
        return Event(
            eventId = eventId,
            publisherId = publisherId,
            participantIds = participantIds,
            eventName = eventName,
            location = location,
            eventDateString = eventDate?.toString() ?: this.eventDateString,
            eventStartTimeString = eventStartTime?.toString() ?: this.eventStartTimeString
        )
    }

    // Computed property based on eventDate and eventStartTime
    @get:Exclude
    val status: EventStatus
        get() {
            if (eventDate == null || eventStartTime == null) {
                return EventStatus.UPCOMING
            }

            val now = LocalDateTime.now()
            val today = now.toLocalDate()

            // If the event is today, it's ongoing
            if (eventDate == today) {
                return EventStatus.ONGOING
            }

            val eventDateTime = LocalDateTime.of(eventDate!!, eventStartTime!!)

            // Assume event duration is 3 hours
            val eventEndDateTime = eventDateTime.plusHours(3)

            return when {
                now.isBefore(eventDateTime) -> EventStatus.UPCOMING
                now.isAfter(eventEndDateTime) -> EventStatus.COMPLETED
                else -> EventStatus.ONGOING
            }
        }

    companion object {
        // Factory method for creating Event with LocalDate/LocalTime
        fun create(
            eventId: String? = null,
            publisherId: String? = null,
            participantIds: List<String>? = null,
            eventName: String? = null,
            location: String? = null,
            eventDate: LocalDate? = null,
            eventStartTime: LocalTime? = null
        ): Event {
            return Event(
                eventId = eventId,
                publisherId = publisherId,
                participantIds = participantIds,
                eventName = eventName,
                location = location,
                eventDateString = eventDate?.toString(),
                eventStartTimeString = eventStartTime?.toString()
            )
        }

        private val names = listOf("Zlatan Stadler", "Gustav Rasslan", "Mikkel Rahbek", "David Sandell", "Oliver Payne")
        private val ids: List<String> = List(names.size) { index -> "user${index + 1}" }

        fun previewEvents(count: Int): List<Event> {
            return List(count) { index ->
                val name = names.random()
                create(
                    eventId = "event$name${index + 1}",
                    location = "Fortuna alle $index",
                    eventName = "Middag hos $name",
                    eventDate = LocalDate.parse("2024-06-15"),
                    eventStartTime = LocalTime.parse("18:00")
                )
            }
        }

        // Static test data for details page
        val staticTestEvents: List<Event> = listOf(
            create(
                eventId = "event1",
                eventName = "Middag hos Zlatan",
                location = "Fortuna alle 1, 2000 Frederiksberg",
                eventDate = LocalDate.parse("2024-12-01"),
                eventStartTime = LocalTime.parse("19:00"),
                participantIds = ids,
            ),
            create(
                eventId = "event2",
                eventName = "Frokost med Gustav",
                location = "Nørrebrogade 2, 2200 København N",
                eventDate = LocalDate.parse("2024-12-05"),
                eventStartTime = LocalTime.parse("12:30"),
                participantIds = ids,
            ),
            create(
                eventId = "event3",
                publisherId = "3",
                eventName = "Brunch hos Mikkel",
                location = "Østerbrogade 3, 2100 København Ø",
                eventDate = LocalDate.parse("2024-12-10"),
                eventStartTime = LocalTime.parse("10:00")
            ),
            create(
                eventId = "event4",
                publisherId = "3",
                eventName = "Brunch hos Mikkel",
                location = "Amagerbrogade 4, 2300 København S",
                eventDate = LocalDate.parse("2024-12-10"),
                eventStartTime = LocalTime.parse("10:00")
            ),
            create(
                eventId = "event5",
                publisherId = "3",
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
