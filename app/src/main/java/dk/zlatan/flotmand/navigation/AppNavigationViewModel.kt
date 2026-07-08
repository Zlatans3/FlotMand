package dk.zlatan.flotmand.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.util.feature_flags.FeatureFlagManager
import dk.zlatan.flotmand.util.feature_flags.FeatureKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AppNavigationViewModel"

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    private val accountService: AccountService,
    private val featureFlagManager: FeatureFlagManager,
) : ViewModel() {

    private val _navigationStack = MutableStateFlow<List<AppDestination>>(listOf(AppDestination.Authentication))
    val navigationStack: StateFlow<List<AppDestination>> = _navigationStack.asStateFlow()

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth: StateFlow<Boolean> = _isCheckingAuth.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized with stack: ${_navigationStack.value}")
        // Initialize navigation based on current auth state
        viewModelScope.launch {
            combine(
                accountService.currentUser,
                featureFlagManager.isEnabled(FeatureKey.FORCE_PROFILE_SETUP),
            ) { user, forceProfileSetup -> user to forceProfileSetup }.collect { (user, forceProfileSetup) ->
                // Check if user is valid: not null, has an ID, and is not anonymous
                val isValidUser = user != null && user.id.isNotEmpty() && !user.isAnonymous
                Log.d(TAG, "Auth state changed - User: ${user?.email ?: "null"}, ID: ${user?.id ?: "null"}, isAnonymous: ${user?.isAnonymous}, isValidUser: $isValidUser")

                // Mark auth check as complete after first emission
                _isCheckingAuth.value = false

                if (user != null && isValidUser) {
                    // profileCompleted is only stamped (as false) on brand-new user docs,
                    // so existing users — where the field is absent/null — never see setup.
                    // The debug flag forces the screen; it is cleared again by
                    // ProfileSetupViewModel when the user continues or skips.
                    val target =
                        if (forceProfileSetup || user.profileCompleted == false) {
                            AppDestination.ProfileSetup
                        } else {
                            AppDestination.MainApp
                        }
                    if (_navigationStack.value.lastOrNull() != target) {
                        Log.d(TAG, "Navigating to $target")
                        _navigationStack.value = listOf(target)
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

