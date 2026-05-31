package dk.zlatan.flotmand.Features.frontpage.host_rotation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.RotationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HostRotationUiState(
    val rotationMembers: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val showAddGhostUserDialog: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HostRotationViewModel @Inject constructor(
    private val rotationService: RotationService,
    private val accountService: AccountService,
) : ViewModel() {

    val uiState: StateFlow<HostRotationUiState> =
        rotationService.observeGroup(GROUP_ID)
            .flatMapLatest { group ->
                if (group == null) {
                    flowOf(HostRotationUiState(isLoading = false))
                } else {
                    flow {
                        val membersById = accountService.getUsersByIds(group.members).associateBy { it.id }
                        val ordered = group.rotationOrder.mapNotNull { membersById[it] }
                        emit(HostRotationUiState(rotationMembers = ordered, isLoading = false))
                    }
                }
            }
            .catch { emit(HostRotationUiState(isLoading = false)) }
            .map { it }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HostRotationUiState(isLoading = true),
            )

    fun onShowAddGhostUserDialog() {
        // Trigger dialog via a separate MutableStateFlow so we don't invalidate the whole uiState
        _showDialog.value = true
    }

    fun onDismissDialog() {
        _showDialog.value = false
    }

    fun onAddGhostUser(name: String) {
        if (name.isBlank()) return
        _showDialog.value = false
        viewModelScope.launch {
            try { rotationService.createGhostUser(GROUP_ID, name) } catch (_: Exception) {}
        }
    }

    fun onRemoveUser(userId: String) {
        viewModelScope.launch {
            try { rotationService.removeUserFromRotation(GROUP_ID, userId) } catch (_: Exception) {}
        }
    }

    fun onSaveOrder(newOrder: List<String>) {
        viewModelScope.launch {
            try { rotationService.reorderRotation(GROUP_ID, newOrder) } catch (_: Exception) {}
        }
    }

    fun onResetPlacements() {
        viewModelScope.launch {
            try { rotationService.resetTimelineOverrides(GROUP_ID) } catch (_: Exception) {}
        }
    }

    private val _showDialog = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    companion object {
        private const val GROUP_ID = "flotmand"
    }
}
