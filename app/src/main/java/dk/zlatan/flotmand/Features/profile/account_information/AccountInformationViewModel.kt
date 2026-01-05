package dk.zlatan.flotmand.Features.profile.account_information

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountInformationViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    val user = accountService.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

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
            } catch (e: Exception) {
                _errorMessage.value = "Kunne ikke opdatere navn: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}