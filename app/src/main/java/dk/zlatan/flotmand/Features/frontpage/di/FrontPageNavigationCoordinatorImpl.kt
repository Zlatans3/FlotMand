package dk.zlatan.flotmand.Features.frontpage.di

import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrontPageNavigationCoordinatorImpl @Inject constructor() : FrontPageNavigationCoordinator {
    private val _navigationStack = MutableStateFlow<List<FrontPageDestination>>(listOf(FrontPageDestination.FrontPageScreen))
    override val navigationStack: StateFlow<List<FrontPageDestination>> = _navigationStack.asStateFlow()

    override fun navigate(destination: FrontPageDestination) {
        _navigationStack.value = _navigationStack.value + destination
    }

    override fun pop() {
        if (_navigationStack.value.isNotEmpty()) {
            _navigationStack.value = _navigationStack.value.dropLast(1)
        }
    }
}