package dk.zlatan.flotmand.Features.polls.navigation

import kotlinx.coroutines.flow.StateFlow

interface PollsNavigationCoordinator {
    val navigationStack: StateFlow<List<PollsDestination>>

    fun navigate(destination: PollsDestination)

    fun pop()

    fun resetToRoot()
}
