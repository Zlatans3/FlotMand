package dk.zlatan.flotmand.Features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val accountService: AccountService
) : ViewModel() {
    val user: StateFlow<User?> = accountService.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    fun signOut() {
        viewModelScope.launch {
            accountService.signOut()
        }
    }
}