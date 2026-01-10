package dk.zlatan.flotmand.Features.my_events.di

import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsDestination
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigationCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyEventsNavigationCoordinatorImpl @Inject constructor() : MyEventsNavigationCoordinator {
    private val _navigationStack = MutableStateFlow<List<MyEventsDestination>>(listOf(MyEventsDestination.MyEvents))
    override val navigationStack: StateFlow<List<MyEventsDestination>> = _navigationStack.asStateFlow()

    override fun navigate(destination: MyEventsDestination) {
        _navigationStack.value = _navigationStack.value + destination
    }

    override fun pop() {
        if (_navigationStack.value.isNotEmpty()) {
            _navigationStack.value = _navigationStack.value.dropLast(1)
        }
    }

    override fun resetToRoot() {
        _navigationStack.value = listOf(MyEventsDestination.MyEvents)
    }
}