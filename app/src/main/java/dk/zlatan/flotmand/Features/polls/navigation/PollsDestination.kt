package dk.zlatan.flotmand.Features.polls.navigation

sealed class PollsDestination {
    data object PollsList : PollsDestination()
    data class PollDetail(val votingId: String) : PollsDestination()
    data class AddEventFromVoting(val votingId: String) : PollsDestination()
}
