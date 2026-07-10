package dk.zlatan.flotmand.Features.my_events.navigaiton

sealed class MyEventsDestination {
    object MyEvents : MyEventsDestination()
    object AddEvent : MyEventsDestination()
    data class EditEvent(val eventId: String) : MyEventsDestination()
    data class AddEventFromVoting(val votingId: String) : MyEventsDestination()
    data class EventDetail(val eventId: String) : MyEventsDestination()
    data class UserDetails(val userId: String) : MyEventsDestination()
}

