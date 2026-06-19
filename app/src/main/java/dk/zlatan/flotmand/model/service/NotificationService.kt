package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationService {
    /**
     * Writes a notification into each listed user's notification collection.
     * Used to broadcast price updates and other host-initiated events to participants.
     */
    suspend fun sendToUsers(userIds: List<String>, notification: AppNotification)
    /** Real-time stream of all notifications for the current user, newest first. */
    val notifications: Flow<List<AppNotification>>

    /** Number of notifications the user hasn't opened yet. */
    val unreadCount: Flow<Int>

    /** Marks one notification as read (called when the user taps it). */
    fun markAsRead(notificationId: String)

    /** Permanently removes a notification (swipe-to-dismiss / X button). */
    fun dismiss(notificationId: String)

    /** Permanently removes all notifications. */
    fun dismissAll()

    /** Marks every unread notification as read. */
    fun markAllAsRead()

    /** Marks any unread notifications whose referenceId matches as read. */
    fun markAsReadByReferenceId(referenceId: String)
}
