package dk.zlatan.flotmand.impl

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dk.zlatan.flotmand.model.AppNotification
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationServiceImpl
    @Inject
    constructor(
        private val accountService: AccountService,
    ) : NotificationService {

        // Singleton-scoped so the shared flow lives as long as the service instance.
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        private fun itemsCollection(uid: String) =
            Firebase.firestore
                .collection(NOTIFICATIONS_COLLECTION)
                .document(uid)
                .collection(ITEMS_COLLECTION)

        // Shared so that FrontPageViewModel (unreadCount) and NotificationsViewModel
        // both read from the same single Firestore listener instead of opening two.
        override val notifications: Flow<List<AppNotification>> = callbackFlow {
            val uid = accountService.currentUserId
            if (uid.isBlank()) {
                trySend(emptyList())
                awaitClose()
                return@callbackFlow
            }

            val listener = itemsCollection(uid)
                .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Log but do NOT close — Firestore retries the listener automatically.
                        // Closing here would kill the flow permanently on transient errors
                        // and crash the ViewModel on PERMISSION_DENIED.
                        Log.w(TAG, "Notifications listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.mapNotNull { it.toObject<AppNotification>() }
                        ?: emptyList()
                    Log.d(TAG, "snapshot: ${items.size} items — ${items.map { "${it.id.take(6)} isRead=${it.isRead}" }}")
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }.shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            replay = 1,
        )

        override val unreadCount: Flow<Int> =
            notifications
                .map { list -> list.count { !it.isRead } }
                .catch { e ->
                    Log.w(TAG, "unreadCount error: ${e.message}")
                    emit(0)
                }

        override fun markAsRead(notificationId: String) {
            val uid = accountService.currentUserId
            if (uid.isBlank()) {
                Log.w(TAG, "markAsRead: skipped — uid is blank")
                return
            }
            Log.d(TAG, "markAsRead: writing isRead=true for id=$notificationId uid=$uid")
            scope.launch {
                try {
                    itemsCollection(uid).document(notificationId).update(IS_READ_FIELD, true).await()
                    Log.d(TAG, "markAsRead: write confirmed for id=$notificationId")
                } catch (e: Exception) {
                    Log.e(TAG, "markAsRead: FAILED for id=$notificationId — ${e.message}", e)
                }
            }
        }

        override fun dismiss(notificationId: String) {
            val uid = accountService.currentUserId
            if (uid.isBlank()) return
            scope.launch {
                try {
                    itemsCollection(uid).document(notificationId).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to dismiss notification: ${e.message}")
                }
            }
        }

        override fun markAllAsRead() {
            val uid = accountService.currentUserId
            if (uid.isBlank()) return
            scope.launch {
                try {
                    val unread = itemsCollection(uid).whereEqualTo(IS_READ_FIELD, false).get().await()
                    if (unread.documents.isEmpty()) return@launch
                    val batch = Firebase.firestore.batch()
                    unread.documents.forEach { doc -> batch.update(doc.reference, IS_READ_FIELD, true) }
                    batch.commit().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to mark all notifications as read: ${e.message}")
                }
            }
        }

        companion object {
            private const val NOTIFICATIONS_COLLECTION = "notifications"
            private const val ITEMS_COLLECTION = "items"
            private const val IS_READ_FIELD = "isRead"
            private const val CREATED_AT_FIELD = "createdAtMillis"
            private const val TAG = "NotificationService"
        }
    }
