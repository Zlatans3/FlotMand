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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
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

        // distinctUntilChanged guards against Firebase re-emitting the same user on token
        // refresh, which would otherwise restart the Firestore listener and emit a stale
        // snapshot before the in-flight markAsRead write lands.
        // flatMapLatest restarts the listener on real auth changes (login / logout).
        // Eagerly + replay=1 keeps the listener permanently active and ensures new
        // subscribers never block waiting for the first emission.
        private val _notifications: SharedFlow<List<AppNotification>> =
            accountService.currentUser
                // Firebase Auth re-emits the same user on token refresh — skip restarts
                // unless the actual uid changes (i.e. a real login / logout).
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .flatMapLatest { user ->
                    val uid = user?.id.orEmpty()
                    if (uid.isBlank()) {
                        flowOf(emptyList())
                    } else {
                        // Bridges Firestore's callback-based API into a coroutine Flow.
                        callbackFlow {
                            val listener = itemsCollection(uid)
                                .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
                                // Fires immediately with the current snapshot, then again on
                                // every document change — giving us real-time updates.
                                .addSnapshotListener { snapshot, error ->
                                    if (error != null) {
                                        Log.w(TAG, "Notifications listener error: ${error.message}")
                                        return@addSnapshotListener
                                    }
                                    val items = snapshot?.documents
                                        ?.mapNotNull { it.toObject<AppNotification>() }
                                        ?: emptyList()
                                    trySend(items)
                                }
                            // Deregisters the Firestore listener when the flow is cancelled
                            // (logout / process death) to avoid memory and network leaks.
                            awaitClose { listener.remove() }
                        }
                    }
                }
                // Turns the cold flow into a hot SharedFlow so all subscribers share one
                // Firestore listener. Eagerly starts immediately; replay=1 delivers the
                // latest list to new subscribers without waiting for the next snapshot.
                .shareIn(
                    scope = scope,
                    started = SharingStarted.Eagerly,
                    replay = 1,
                )

        override val notifications: Flow<List<AppNotification>> = _notifications

        private val locallyReadIds = MutableStateFlow<Set<String>>(emptySet())

        override val unreadCount: StateFlow<Int> =
            combine(notifications, locallyReadIds) { list, readIds ->
                list.count { !it.isRead && it.id !in readIds }
            }
                .catch { e ->
                    Log.w(TAG, "unreadCount error: ${e.message}")
                    emit(0)
                }
                .stateIn(scope, SharingStarted.WhileSubscribed(5_000), 0)

        override fun markAsRead(notificationId: String) {
            locallyReadIds.update { it + notificationId }
            val uid = accountService.currentUserId
            if (uid.isBlank()) {
                Log.w(TAG, "markAsRead: skipped — uid is blank")
                return
            }
            scope.launch {
                try {
                    itemsCollection(uid).document(notificationId).update(IS_READ_FIELD, true).await()
                } catch (e: Exception) {
                    Log.e(TAG, "markAsRead: FAILED for id=$notificationId — ${e.message}", e)
                }
            }
        }

        // Note that dismissals are permanent
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

        override fun markAsReadByReferenceId(referenceId: String) {
            val uid = accountService.currentUserId
            if (uid.isBlank()) return
            scope.launch {
                try {
                    val docs = itemsCollection(uid)
                        .whereEqualTo(REFERENCE_ID_FIELD, referenceId)
                        .get()
                        .await()
                    if (docs.documents.isEmpty()) return@launch
                    val batch = Firebase.firestore.batch()
                    docs.documents.forEach { doc -> batch.update(doc.reference, IS_READ_FIELD, true) }
                    batch.commit().await()
                } catch (e: Exception) {
                    Log.w(TAG, "markAsReadByReferenceId: FAILED for referenceId=$referenceId — ${e.message}")
                }
            }
        }

        override fun dismissAll() {
            val uid = accountService.currentUserId
            if (uid.isBlank()) return
            scope.launch {
                try {
                    val docs = itemsCollection(uid).get().await()
                    if (docs.documents.isEmpty()) return@launch
                    val batch = Firebase.firestore.batch()
                    docs.documents.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to dismiss all notifications: ${e.message}")
                }
            }
        }

        override suspend fun sendToUsers(userIds: List<String>, notification: AppNotification) {
            if (userIds.isEmpty()) {
                Log.w(TAG, "sendToUsers: recipient list is empty, skipping")
                return
            }
            Log.d(TAG, "sendToUsers: sending to ${userIds.size} user(s): $userIds")
            try {
                val db = Firebase.firestore
                val batch = db.batch()
                userIds.forEach { uid ->
                    val docRef = itemsCollection(uid).document()
                    batch.set(docRef, notification)
                }
                batch.commit().await()
                Log.d(TAG, "sendToUsers: batch committed successfully")
            } catch (e: Exception) {
                // Most likely cause: Firestore security rules deny writes to other users'
                // notification collections. Fix: allow `create` for any authenticated user
                // in your Firestore rules (see README or inline comment below).
                //
                // match /notifications/{userId}/items/{itemId} {
                //   allow read, update, delete: if request.auth != null && request.auth.uid == userId;
                //   allow create: if request.auth != null;
                // }
                Log.e(TAG, "sendToUsers: FAILED (check Firestore rules) — ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }

        override fun markAllAsRead() {
            val currentIds = _notifications.replayCache.firstOrNull()?.map { it.id }?.toSet().orEmpty()
            locallyReadIds.update { it + currentIds }
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
            private const val REFERENCE_ID_FIELD = "referenceId"
            private const val CREATED_AT_FIELD = "createdAtMillis"
            private const val TAG = "NotificationService"
        }
    }
