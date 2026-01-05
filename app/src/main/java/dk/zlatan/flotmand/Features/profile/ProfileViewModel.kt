package dk.zlatan.flotmand.Features.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.Features.FmAppViewModel
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val accountService: AccountService
) : FmAppViewModel() {
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
            accountService.signOut()
            _signedOut.value = true
            _signOutLoading.value = false
        }
    }
}