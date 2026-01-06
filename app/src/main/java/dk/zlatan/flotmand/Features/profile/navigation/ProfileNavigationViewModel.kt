package dk.zlatan.flotmand.Features.profile.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileNavigationViewModel @Inject constructor(
    private val profileNavigationCoordinator: ProfileNavigationCoordinator
) : ViewModel() {
    val navigationStack: StateFlow<List<ProfileDestination>> = profileNavigationCoordinator.navigationStack

    fun navigate(destination: ProfileDestination) {
        profileNavigationCoordinator.navigate(destination)
    }

    fun pop() {
        profileNavigationCoordinator.pop()
    }
}