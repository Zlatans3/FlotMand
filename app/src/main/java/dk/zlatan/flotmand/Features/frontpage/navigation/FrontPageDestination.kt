package dk.zlatan.flotmand.Features.frontpage.navigation

sealed class FrontPageDestination {
    data object FrontPageScreen : FrontPageDestination()
    data class EventDetail(val eventId: String) : FrontPageDestination()
}
