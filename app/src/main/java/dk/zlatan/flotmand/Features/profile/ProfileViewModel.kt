package dk.zlatan.flotmand.Features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.Features.NotesAppViewModel
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val accountService: AccountService
) : NotesAppViewModel() {
    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut.asStateFlow()

    private val _signOutLoading = MutableStateFlow(false)
    val signOutLoading: StateFlow<Boolean> = _signOutLoading.asStateFlow()

    init {
        launchCatching {
            _user.value = accountService.getUserProfile()
        }
    }

    fun onUpdateDisplayNameClick(newDisplayName: String) {
        launchCatching {
            accountService.updateDisplayName(newDisplayName)
            _user.value = accountService.getUserProfile()
        }
    }

    fun signOut() {
        launchCatching {
            _signOutLoading.value = true
            accountService.signOut() // This should be a suspend function that completes when sign out is done
            _signedOut.value = true
            _signOutLoading.value = false
        }
    }
}