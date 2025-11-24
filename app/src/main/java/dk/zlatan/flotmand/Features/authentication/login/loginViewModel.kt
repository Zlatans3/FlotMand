package dk.zlatan.flotmand.Features.authentication.login

import android.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.service.AccountService
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
    private val accountService: AccountService
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

//    fun onSignInWithGoogle(credential: Credential, openAndPopUp: (String, String) -> Unit) {
//        launchCatching {
//            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
//                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
//                accountService.signInWithGoogle(googleIdTokenCredential.idToken)
//                openAndPopUp(NOTES_LIST_SCREEN, SIGN_IN_SCREEN)
//            } else {
//                Log.e(ERROR_TAG, UNEXPECTED_CREDENTIAL)
//            }
//        }
//    }
}