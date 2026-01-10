package dk.zlatan.flotmand.Features.frontpage.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FrontPageNavigationViewModel @Inject constructor(
    private val frontPageNavigationCoordinator: FrontPageNavigationCoordinator
) : ViewModel() {
    val navigationStack: StateFlow<List<FrontPageDestination>> = frontPageNavigationCoordinator.navigationStack

    fun navigate(destination: FrontPageDestination) {
        frontPageNavigationCoordinator.navigate(destination)
    }

    fun pop() {
        frontPageNavigationCoordinator.pop()
    }

    fun resetToRoot() {
        frontPageNavigationCoordinator.resetToRoot()
    }
}

