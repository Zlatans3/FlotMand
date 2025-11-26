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
        viewModelScope.launch {
            accountService.signOut()
        }
    }
}