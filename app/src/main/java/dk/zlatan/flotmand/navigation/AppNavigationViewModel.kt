package dk.zlatan.flotmand.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AppNavigationViewModel"

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    private val _navigationStack = MutableStateFlow<List<AppDestination>>(listOf(AppDestination.Authentication))
    val navigationStack: StateFlow<List<AppDestination>> = _navigationStack.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized with stack: ${_navigationStack.value}")
        // Initialize navigation based on current auth state
        viewModelScope.launch {
            accountService.currentUser.collect { user ->
                // Check if user is valid: not null, has an ID, and is not anonymous
                val isValidUser = user != null && user.id.isNotEmpty() && !user.isAnonymous
                Log.d(TAG, "Auth state changed - User: ${user?.email ?: "null"}, ID: ${user?.id ?: "null"}, isAnonymous: ${user?.isAnonymous}, isValidUser: $isValidUser")

                if (isValidUser) {
                    // User is logged in, navigate to main app
                    if (_navigationStack.value.isEmpty() || _navigationStack.value.last() != AppDestination.MainApp) {
                        Log.d(TAG, "Navigating to MainApp")
                        _navigationStack.value = listOf(AppDestination.MainApp)
                    }
                } else {
                    // User is not logged in, show authentication
                    if (_navigationStack.value.isEmpty() || _navigationStack.value.last() != AppDestination.Authentication) {
                        Log.d(TAG, "Navigating to Authentication")
                        _navigationStack.value = listOf(AppDestination.Authentication)
                    }
                }
                Log.d(TAG, "Navigation stack is now: ${_navigationStack.value}")
            }
        }
    }

    fun navigate(destination: AppDestination) {
        _navigationStack.value = listOf(destination)
    }

    fun navigateToMainApp() {
        navigate(AppDestination.MainApp)
    }

    fun navigateToAuthentication() {
        navigate(AppDestination.Authentication)
    }

    fun pop() {
        if (_navigationStack.value.size > 1) {
            _navigationStack.value = _navigationStack.value.dropLast(1)
        }
    }
}

