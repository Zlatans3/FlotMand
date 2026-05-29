package dk.zlatan.flotmand.Features.frontpage.navigation

sealed class FrontPageDestination {
    data object FrontPageScreen : FrontPageDestination()
    data class EventDetail(val eventId: String) : FrontPageDestination()
    data class VotingDetail(val votingId: String) : FrontPageDestination()
    data object DateVoting : FrontPageDestination()
    data object HostRotation : FrontPageDestination()
    data class AddEventFromVoting(val votingId: String) : FrontPageDestination()
    data class EditEvent(val eventId: String) : FrontPageDestination()
    data object Notifications : FrontPageDestination()
}
