package dk.zlatan.flotmand

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.Features.authentication.di.AuthenticationNavigation
import dk.zlatan.flotmand.Features.frontpage.di.FrontPageNavigation
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigation
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigation
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class MainRoute {
    object FrontPage : MainRoute()
    object MyEvents : MainRoute()
    object Profile : MainRoute()
    object Authentication : MainRoute()
}

// UI state data class
data class MainUiState(
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val dinnerEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val accountService: AccountService,
    private val dinnerEventService: DinnerEventService
) : ViewModel() {

    val items: List<MainRoute> by lazy {
        listOf(
            MainRoute.FrontPage,
            MainRoute.MyEvents,
            MainRoute.Profile,
            MainRoute.Authentication
        )
    }

    // Combine all relevant state into a single StateFlow
    val uiState: StateFlow<MainUiState> = combine(
        accountService.currentUser,
        dinnerEventService.dinnerEventsByUserId
    ) { user, events ->
        MainUiState(
            isLoggedIn = user != null,
            user = user,
            dinnerEvents = events
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainUiState()
    )

    @Composable
    fun ItemContent(
        item: MainRoute,
        modifier: Modifier = Modifier
    ) {
        when (item) {
            is MainRoute.FrontPage -> {
                FrontPageNavigation()
            }
            is MainRoute.MyEvents -> {
                MyEventsNavigation()
            }
            is MainRoute.Profile -> {
                ProfileNavigation()
            }
            is MainRoute.Authentication -> {
                AuthenticationNavigation()
            }
        }
    }

    // Login and logout actions
    fun login(email: String, password: String) {
        viewModelScope.launch {
            accountService.signIn(email, password)
        }
    }

    fun logout() {
        viewModelScope.launch {
            accountService.signOut()
        }
    }
}