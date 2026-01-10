package dk.zlatan.flotmand.Features.frontpage.navigation

sealed class FrontPageDestination {
    data object FrontPageScreen : FrontPageDestination()
    data class EventDetail(val eventId: String) : FrontPageDestination()
    data object VotingDetail : FrontPageDestination()
    data class DateVoting(val eventId: String? = null) : FrontPageDestination()
    data object HostRotation : FrontPageDestination()

}
