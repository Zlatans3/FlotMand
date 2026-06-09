package dk.zlatan.flotmand.Features.frontpage.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.AppNotification
import dk.zlatan.flotmand.model.service.NotificationService
import kotlinx.coroutines.delay
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
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class NotificationsViewModel
    @Inject
    constructor(
        private val notificationService: NotificationService,
    ) : ViewModel() {
        private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
        private val _isLoading = MutableStateFlow(true)
        private val _isRefreshing = MutableStateFlow(false)

        val uiState: StateFlow<NotificationsUiState> =
            combine(_notifications, _isLoading, _isRefreshing) { notifications, isLoading, isRefreshing ->
                NotificationsUiState(notifications = notifications, isLoading = isLoading, isRefreshing = isRefreshing)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                NotificationsUiState(),
            )

        init {
            viewModelScope.launch {
                notificationService.notifications
                    .catch { _isLoading.update { false } }
                    .collect { serverNotifications ->
                        serverNotifications.forEach { n ->
                            android.util.Log.d(
                                "NotificationsVM",
                                "id=${n.id} senderPhotoUrl='${n.senderPhotoUrl}' senderDisplayName='${n.senderDisplayName}'",
                            )
                        }
                        _notifications.update { current ->
                            // Preserve optimistic reads: once marked read locally,
                            // don't let a stale snapshot revert it back to unread.
                            val locallyRead = current.filter { it.isRead }.map { it.id }.toSet()
                            serverNotifications.map { notif ->
                                if (notif.id in locallyRead && !notif.isRead) notif.copy(isRead = true)
                                else notif
                            }
                        }
                        _isLoading.update { false }
                    }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.update { true }
                delay(800)
                _isRefreshing.update { false }
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

        fun dismissAll() {
            _notifications.update { emptyList() }
            notificationService.dismissAll()
        }

        fun markAsRead(notificationId: String) {
            _notifications.update { current ->
                current.map { if (it.id == notificationId) it.copy(isRead = true) else it }
            }
            notificationService.markAsRead(notificationId)
        }
    }
