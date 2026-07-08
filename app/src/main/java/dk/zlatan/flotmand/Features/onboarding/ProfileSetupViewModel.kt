package dk.zlatan.flotmand.Features.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.util.StringProvider
import dk.zlatan.flotmand.util.feature_flags.FeatureFlagManager
import dk.zlatan.flotmand.util.feature_flags.FeatureKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSetupUiState(
    val displayName: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProfileSetupViewModel
    @Inject
    constructor(
        private val accountService: AccountService,
        private val stringProvider: StringProvider,
        private val featureFlagManager: FeatureFlagManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileSetupUiState())
        val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

        init {
            val profile = accountService.getUserProfile()
            _uiState.update {
                it.copy(
                    displayName = profile.displayName,
                    photoUrl = profile.photoUrl,
                )
            }
            // Phone and bio live in Firestore, not on the Auth profile — fetch them
            // so returning users see what they already have. Only fill fields that
            // are still blank, in case the user started typing before this lands.
            viewModelScope.launch {
                val firestoreUser = accountService.getUserById(accountService.currentUserId) ?: return@launch
                _uiState.update { current ->
                    current.copy(
                        displayName = current.displayName.ifBlank { firestoreUser.displayName },
                        phoneNumber = current.phoneNumber.ifBlank { firestoreUser.phoneNumber },
                        bio = current.bio.ifBlank { firestoreUser.bio },
                        photoUrl = current.photoUrl.ifBlank { firestoreUser.photoUrl },
                    )
                }
            }
        }

        fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value) }

        fun onPhoneNumberChange(value: String) = _uiState.update { it.copy(phoneNumber = value) }

        fun onBioChange(value: String) = _uiState.update { it.copy(bio = value.take(User.BIO_MAX_LENGTH)) }

        /**
         * Persists the filled-in fields and stamps the profileCompleted flag.
         * Navigation away happens reactively in AppNavigationViewModel, so isSaving
         * stays true on success to keep the buttons disabled until the screen swaps.
         */
        fun saveAndContinue() {
            val state = _uiState.value
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                try {
                    val trimmedName = state.displayName.trim()
                    if (trimmedName.isNotBlank() &&
                        trimmedName != accountService.getUserProfile().displayName
                    ) {
                        accountService.updateDisplayName(trimmedName)
                    }
                    val trimmedPhone = state.phoneNumber.trim()
                    if (trimmedPhone.isNotBlank()) {
                        accountService.updatePhoneNumber(trimmedPhone)
                    }
                    if (state.bio.isNotBlank()) {
                        accountService.updateBio(state.bio)
                    }
                    accountService.markProfileCompleted()
                    featureFlagManager.disable(FeatureKey.FORCE_PROFILE_SETUP)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save profile setup: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = stringProvider.getString(R.string.profile_setup_error),
                        )
                    }
                }
            }
        }

        /** Skipping only stamps the flag — nothing is saved and no loading state is shown. */
        fun skip() {
            viewModelScope.launch {
                try {
                    accountService.markProfileCompleted()
                    featureFlagManager.disable(FeatureKey.FORCE_PROFILE_SETUP)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to skip profile setup: ${e.message}", e)
                }
            }
        }

        companion object {
            private const val TAG = "ProfileSetupViewModel"
        }
    }
