package dk.zlatan.flotmand.Features.authentication.di

import dk.zlatan.flotmand.Features.authentication.navigation.AuthenticationDestination
import dk.zlatan.flotmand.Features.authentication.navigation.AuthenticationNavigationCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationNavigationCoordinatorImpl @Inject constructor() : AuthenticationNavigationCoordinator {
    private val _navigationStack = MutableStateFlow<List<AuthenticationDestination>>(listOf(AuthenticationDestination.Login))
    override val navigationStack: StateFlow<List<AuthenticationDestination>> = _navigationStack.asStateFlow()

    override fun navigate(destination: AuthenticationDestination) {
        _navigationStack.value = _navigationStack.value + destination
    }

    override fun pop() {
        if (_navigationStack.value.isNotEmpty()) {
            _navigationStack.value = _navigationStack.value.dropLast(1)
        }
    }
}

