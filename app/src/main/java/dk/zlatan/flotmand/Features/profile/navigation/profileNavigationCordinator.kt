package dk.zlatan.flotmand.Features.profile.navigation

import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton coordinator used to navigate inside the profile tab.
 * Should only be used by top level navigation and deep links.
 */
interface ProfileNavigationCoordinator {
    val navigationStack: StateFlow<List<ProfileDestination>>

    fun navigate(destination: ProfileDestination)
    fun pop()
}