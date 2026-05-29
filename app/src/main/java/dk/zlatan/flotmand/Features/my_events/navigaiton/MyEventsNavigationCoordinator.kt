package dk.zlatan.flotmand.Features.my_events.navigaiton

import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton coordinator used to navigate inside the my events tab.
 * Should only be used by top level navigation and deep links.
 */
interface MyEventsNavigationCoordinator {
    val navigationStack: StateFlow<List<MyEventsDestination>>

    fun navigate(destination: MyEventsDestination)

    fun pop()

    fun resetToRoot()
}
