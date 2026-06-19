package dk.zlatan.flotmand.Features.authentication.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthenticationNavigationViewModel @Inject constructor(
    private val authenticationNavigationCoordinator: AuthenticationNavigationCoordinator
) : ViewModel() {
    val navigationStack: StateFlow<List<AuthenticationDestination>> = authenticationNavigationCoordinator.navigationStack

    fun navigate(destination: AuthenticationDestination) {
        authenticationNavigationCoordinator.navigate(destination)
    }

    fun pop() {
        authenticationNavigationCoordinator.pop()
    }
}

