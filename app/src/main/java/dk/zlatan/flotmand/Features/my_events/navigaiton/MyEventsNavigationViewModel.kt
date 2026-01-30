package dk.zlatan.flotmand.Features.my_events.navigaiton

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MyEventsNavigationViewModel @Inject constructor(
    private val myEventsNavigationCoordinator: MyEventsNavigationCoordinator
) : ViewModel() {
    val navigationStack: StateFlow<List<MyEventsDestination>> = myEventsNavigationCoordinator.navigationStack

    fun navigate(destination: MyEventsDestination) {
        myEventsNavigationCoordinator.navigate(destination)
    }

    fun pop() {
        myEventsNavigationCoordinator.pop()
    }

    fun resetToRoot() {
        myEventsNavigationCoordinator.resetToRoot()
    }
}

