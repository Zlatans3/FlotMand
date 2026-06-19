package dk.zlatan.flotmand.Features.profile

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.Features.FmAppViewModel
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUiState(
    val eventsHosted: Int = 0,
    val eventsAttended: Int = 0,
    val totalEvents: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val dinnerEventService: DinnerEventService,
) : FmAppViewModel() {
    val user: StateFlow<User> = accountService.currentUser
        .map { it ?: User() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), User())

    private val _signOutLoading = MutableStateFlow(false)
    val signOutLoading: StateFlow<Boolean> = _signOutLoading.asStateFlow()

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    val uiState: StateFlow<ProfileUiState> =
        combine(
            dinnerEventService.allDinnerEvents,
            accountService.currentUser,
        ) { allEvents, currentUser ->
            val uid = currentUser?.id.orEmpty()
            ProfileUiState(
                eventsHosted = allEvents.count {
                    it.publisherId == uid && it.status == EventStatus.COMPLETED
                },
                eventsAttended = allEvents.count {
                    it.status == EventStatus.COMPLETED &&
                        it.publisherId != uid &&
                        it.participantIds?.contains(uid) == true
                },
                totalEvents = allEvents.size,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun updateProfilePhoto(uri: Uri) {
        launchCatching {
            _isUploadingPhoto.value = true
            accountService.updateProfilePhoto(uri)
            _isUploadingPhoto.value = false
        }
    }

    fun signOut() {
        launchCatching {
            _signOutLoading.value = true
            accountService.signOut()
            _signOutLoading.value = false
        }
    }
}
