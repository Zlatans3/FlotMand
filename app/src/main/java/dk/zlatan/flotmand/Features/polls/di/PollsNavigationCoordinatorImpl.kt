package dk.zlatan.flotmand.Features.polls.di

import dk.zlatan.flotmand.Features.polls.navigation.PollsDestination
import dk.zlatan.flotmand.Features.polls.navigation.PollsNavigationCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollsNavigationCoordinatorImpl @Inject constructor() : PollsNavigationCoordinator {
    private val _navigationStack = MutableStateFlow<List<PollsDestination>>(listOf(PollsDestination.PollsList))
    override val navigationStack: StateFlow<List<PollsDestination>> = _navigationStack.asStateFlow()

    override fun navigate(destination: PollsDestination) {
        _navigationStack.value = _navigationStack.value + destination
    }

    override fun pop() {
        if (_navigationStack.value.size > 1) {
            _navigationStack.value = _navigationStack.value.dropLast(1)
        }
    }

    override fun resetToRoot() {
        _navigationStack.value = listOf(PollsDestination.PollsList)
    }
}
