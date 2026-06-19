package dk.zlatan.flotmand.Features.profile.di

import dk.zlatan.flotmand.Features.profile.navigation.ProfileDestination
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigationCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileNavigationCoordinatorImpl @Inject constructor() : ProfileNavigationCoordinator {
    private val _navigationStack = MutableStateFlow<List<ProfileDestination>>(listOf(ProfileDestination.ProfileScreen))
    override val navigationStack: StateFlow<List<ProfileDestination>> = _navigationStack.asStateFlow()

    override fun navigate(destination: ProfileDestination) {
        _navigationStack.value = _navigationStack.value + destination
    }

    override fun pop() {
        if (_navigationStack.value.isNotEmpty()) {
            _navigationStack.value = _navigationStack.value.dropLast(1)
        }
    }

    override fun resetToRoot() {
        _navigationStack.value = listOf(ProfileDestination.ProfileScreen)
    }
}