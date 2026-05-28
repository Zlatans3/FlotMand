package dk.zlatan.flotmand.Features.frontpage.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.AppNotification
import dk.zlatan.flotmand.model.service.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class NotificationsViewModel
    @Inject
    constructor(
        private val notificationService: NotificationService,
    ) : ViewModel() {
        private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
        private val _isLoading = MutableStateFlow(true)

        val uiState: StateFlow<NotificationsUiState> =
            combine(_notifications, _isLoading) { notifications, isLoading ->
                NotificationsUiState(notifications = notifications, isLoading = isLoading)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                NotificationsUiState(),
            )

        init {
            viewModelScope.launch {
                notificationService.notifications
                    .catch { _isLoading.update { false } }
                    .collect { notifications ->
                        Log.d(TAG, "markAsRead update: ${notifications.map { "${it.id.take(6)} isRead=${it.isRead}" }}")
                        _notifications.update { notifications }
                        _isLoading.update { false }
                    }
            }
        }

        fun dismiss(notificationId: String) {
            _notifications.update { current -> current.filter { it.id != notificationId } }
            notificationService.dismiss(notificationId)
        }

        fun markAllAsRead() {
            _notifications.update { current -> current.map { it.copy(isRead = true) } }
            notificationService.markAllAsRead()
        }

        fun markAsRead(notificationId: String) {
            Log.d(TAG, "markAsRead: optimistic update for id=$notificationId")
            _notifications.update { current ->
                current.map { if (it.id == notificationId) it.copy(isRead = true) else it }
            }
            notificationService.markAsRead(notificationId)
        }

        companion object {
            private const val TAG = "NotificationsViewModel"
        }
    }
