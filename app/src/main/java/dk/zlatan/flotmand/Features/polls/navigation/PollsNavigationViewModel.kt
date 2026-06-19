package dk.zlatan.flotmand.Features.polls.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PollsNavigationViewModel @Inject constructor(
    private val coordinator: PollsNavigationCoordinator,
) : ViewModel() {
    val navigationStack: StateFlow<List<PollsDestination>> = coordinator.navigationStack

    fun navigate(destination: PollsDestination) {
        coordinator.navigate(destination)
    }

    fun pop() {
        coordinator.pop()
    }

    fun resetToRoot() {
        coordinator.resetToRoot()
    }
}
