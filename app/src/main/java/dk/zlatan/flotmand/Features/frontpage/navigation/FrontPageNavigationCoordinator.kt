package dk.zlatan.flotmand.Features.frontpage.navigation

import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton coordinator used to navigate inside the front page tab.
 * Should only be used by top level navigation and deep links.
 */
interface FrontPageNavigationCoordinator {
    val navigationStack: StateFlow<List<FrontPageDestination>>

    fun navigate(destination: FrontPageDestination)
    fun pop()
}

