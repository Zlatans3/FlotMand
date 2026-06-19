@file:Suppress("ktlint:standard:package-name")

package dk.zlatan.flotmand.Features.frontpage.host_rotation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.RotationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HostRotationUiState(
    val rotationMembers: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val showAddGhostUserDialog: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HostRotationViewModel @Inject constructor(
    private val rotationService: RotationService,
    private val accountService: AccountService,
) : ViewModel() {

    private val showDialogFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)

    private val rotationFlow =
        rotationService
            .observeGroup(GROUP_ID)
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

    val uiState: StateFlow<HostRotationUiState> =
        combine(rotationFlow, showDialogFlow, errorFlow) { state, showDialog, error ->
            state.copy(showAddGhostUserDialog = showDialog, errorMessage = error)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HostRotationUiState(),
        )

    fun onShowAddGhostUserDialog() {
        showDialogFlow.value = true
    }

    fun onDismissDialog() {
        showDialogFlow.value = false
    }

    fun onAddGhostUser(name: String) {
        if (name.isBlank()) return
        showDialogFlow.value = false
        viewModelScope.launch {
            try {
                rotationService.createGhostUser(GROUP_ID, name)
            } catch (_: Exception) {
            }
        }
    }

    fun onRemoveUser(userId: String) {
        viewModelScope.launch {
            try {
                rotationService.removeUserFromRotation(GROUP_ID, userId)
            } catch (_: Exception) {
            }
        }
    }

    fun onSaveOrder(newOrder: List<String>) {
        viewModelScope.launch {
            try {
                rotationService.reorderRotation(GROUP_ID, newOrder)
            } catch (_: Exception) {
            }
        }
    }

    fun onResetPlacements() {
        viewModelScope.launch {
            try {
                rotationService.resetTimelineOverrides(GROUP_ID)
            } catch (e: Exception) {
                Log.e(TAG, "resetTimelineOverrides failed", e)
                errorFlow.value = e.message ?: "Ukendt fejl"
            }
        }
    }

    fun onErrorDismissed() {
        errorFlow.value = null
    }

    companion object {
        private const val GROUP_ID = "flotmand"
        private const val TAG = "HostRotationVM"
    }
}
