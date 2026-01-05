package dk.zlatan.flotmand.Features.profile.account_information

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountInformationUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AccountInformationViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AccountInformationUiState> = combine(
        accountService.currentUser,
        _isLoading,
        _errorMessage
    ) { user, isLoading, errorMessage ->
        AccountInformationUiState(
            user = user,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountInformationUiState()
    )

    fun updateDisplayName(newDisplayName: String) {
        if (newDisplayName.isBlank()) {
            _errorMessage.value = "Navn kan ikke være tomt"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                accountService.updateDisplayName(newDisplayName)
                // Reload user to trigger AuthStateListener and update UI
                accountService.reloadUser()
            } catch (e: Exception) {
                _errorMessage.value = "Kunne ikke opdatere navn: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePhoneNumber(newPhoneNumber: String) {
        // Basic validation
        if (newPhoneNumber.isNotBlank() && !isValidPhoneNumber(newPhoneNumber)) {
            _errorMessage.value = "Indtast et gyldigt telefonnummer (f.eks. +45 12 34 56 78)"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                accountService.updatePhoneNumber(newPhoneNumber)
                // Reload user to trigger AuthStateListener and update UI
                accountService.reloadUser()
            } catch (e: UnsupportedOperationException) {
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _errorMessage.value = "Kunne ikke opdatere telefonnummer: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation: should start with + and contain only digits, spaces, and +
        val cleaned = phoneNumber.replace(" ", "").replace("-", "")
        return cleaned.matches(Regex("^\\+?[0-9]{8,15}$"))
    }

    fun clearError() {
        _errorMessage.value = null
    }
}