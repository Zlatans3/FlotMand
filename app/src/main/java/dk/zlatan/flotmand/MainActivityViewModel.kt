package dk.zlatan.flotmand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    // Combine all relevant state into a single StateFlow
    val uiState: StateFlow<MainUiState> = combine(
        accountService.currentUser,
        dinnerEventService.dinnerEvents
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