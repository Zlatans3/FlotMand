package dk.zlatan.flotmand.Features.authentication.navigation

import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton coordinator used to navigate inside the authentication flow.
 * Should only be used by top level navigation and deep links.
 */
interface AuthenticationNavigationCoordinator {
    val navigationStack: StateFlow<List<AuthenticationDestination>>

    fun navigate(destination: AuthenticationDestination)
    fun pop()
}

