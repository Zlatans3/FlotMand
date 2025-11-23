package dk.zlatan.flotmand.Features.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.util.GoogleAuthUiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI state data class for Compose
data class LoginUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleAuthUiClient: GoogleAuthUiClient
) : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    // State for login result
    private val _loginState = MutableStateFlow<Boolean?>(null)
    val loginState: StateFlow<Boolean?> = _loginState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Combined UI state (example)
    val uiState = combine(
        loginState,
        isLoading,
        errorMessage
    ) { login,
        loading,
        error ->


        LoginUiState(
            isLoggedIn = login == true,
            isLoading = loading,
            errorMessage = error
        )
    }

    // Listen to FirebaseAuth state changes
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _loginState.value = auth.currentUser != null
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
        // Set initial state
        _loginState.value = firebaseAuth.currentUser != null
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    fun onGoogleLogin(idToken: String) {
        // Reset state before starting login
        _isLoading.value = true
        _errorMessage.value = null
        _loginState.value = null
        viewModelScope.launch {
            try {
                val result = googleAuthUiClient.firebaseSignInWithGoogle(idToken)
                _loginState.value = result
                if (!result) {
                    _errorMessage.value = "Login failed. Please try again."
                }
            } catch (e: Exception) {
                _loginState.value = false
                _errorMessage.value = e.localizedMessage ?: "Unknown error occurred."
            } finally {
                _isLoading.value = false
            }
        }
    }
}