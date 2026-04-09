package dk.zlatan.flotmand.Features.authentication.login

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.Features.FmAppViewModel
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.util.StringProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

// UI state data class for Compose
data class LoginUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val accountService: AccountService,
        private val stringProvider: StringProvider,
    ) : FmAppViewModel() {
        private val firebaseAuth = FirebaseAuth.getInstance()

        // State for login result
        private val loginState: MutableStateFlow<Boolean?> = MutableStateFlow<Boolean?>(null)

        private val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

        private val errorMessage: MutableStateFlow<String?> = MutableStateFlow<String?>(null)

        // Combined UI state (example)
        val uiState =
            combine(
                loginState,
                isLoading,
                errorMessage,
            ) {
                login,
                loading,
                error,
                ->

                LoginUiState(
                    isLoggedIn = login == true,
                    isLoading = loading,
                    errorMessage = error,
                )
            }

        // Listen to FirebaseAuth state changes
        private val authStateListener =
            FirebaseAuth.AuthStateListener { auth ->
                loginState.value = auth.currentUser != null
            }

        init {
            firebaseAuth.addAuthStateListener(authStateListener)
            // Set initial state
            loginState.value = firebaseAuth.currentUser != null
        }

        override fun onCleared() {
            super.onCleared()
            firebaseAuth.removeAuthStateListener(authStateListener)
        }

        fun onSignInWithGoogle(credential: Credential) {
            Log.d("LOGIN_DEBUG", "Credential class: ${credential::class.java.name}")
            isLoading.value = true
            launchCatching {
                try {
                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        accountService.signInWithGoogle(googleIdTokenCredential.idToken)
                        errorMessage.value = null
                    } else {
                        // Log the credential type and details for debugging
                        errorMessage.value =
                            stringProvider.getString(
                                R.string.error_unexpected_credential_type,
                                credential::class.java.simpleName,
                            )
                        Log.e("ERROR_TAG", "UNEXPECTED_CREDENTIAL: $credential")
                    }
                } catch (e: Exception) {
                    errorMessage.value =
                        e.localizedMessage ?: stringProvider.getString(R.string.error_sign_in_generic)
                    Log.e("ERROR_TAG", errorMessage.value!!, e)
                } finally {
                    isLoading.value = false
                }
            }
        }

        fun clearError() {
            errorMessage.value = null
        }

        fun setError(message: String) {
            errorMessage.value = message
        }
    }
