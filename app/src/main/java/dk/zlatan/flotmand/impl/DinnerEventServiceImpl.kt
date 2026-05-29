package dk.zlatan.flotmand.impl

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DinnerEventServiceImpl
    @Inject
    constructor(
        private val auth: AccountService,
    ) : DinnerEventService {
        // Fetch all dinner events
        override val allDinnerEvents: Flow<List<Event>>
            get() =
                callbackFlow {
                    val listenerRegistration =
                        Firebase.firestore
                            .collection(DINNER_EVENTS_COLLECTION)
                            .addSnapshotListener { snapshot, error ->
                                if (error != null) {
                                    Log.e(TAG, "Error fetching events: ${error.message}", error)
                                    close(error)
                                    return@addSnapshotListener
                                }

                                val events =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        safeParseEvent(doc).also { event ->
                                            if (event == null) Log.w(TAG, "Failed to parse document ${doc.id}")
                                        }
                                    } ?: emptyList()

                                Log.d(TAG, "Fetched ${events.size} events")
                                trySend(events)
                            }

                    awaitClose { listenerRegistration.remove() }
                }

        // Fetch only current user's dinner events (for my events page)
        @OptIn(ExperimentalCoroutinesApi::class)
        override val dinnerEventsByUserId: Flow<List<Event>>
            get() =
                auth.currentUser.flatMapLatest { user ->
                    callbackFlow {
                        val listenerRegistration =
                            Firebase.firestore
                                .collection(DINNER_EVENTS_COLLECTION)
                                .whereEqualTo(USER_ID_FIELD, user?.id)
                                .addSnapshotListener { snapshot, error ->
                                    if (error != null) {
                                        Log.e(TAG, "Error fetching user events: ${error.message}", error)
                                        close(error)
                                        return@addSnapshotListener
                                    }

                                    val events =
                                        snapshot?.documents?.mapNotNull { safeParseEvent(it) } ?: emptyList()
                                    Log.d(TAG, "Fetched ${events.size} events for user ${user?.id}")
                                    trySend(events)
                                }

                        awaitClose { listenerRegistration.remove() }
                    }
                }

        /**
         * Safely parse an Event from a DocumentSnapshot, handling the @DocumentId conflict.
         * If the document has an 'eventId' field, we parse manually to avoid the conflict.
         */
        @Suppress("UNCHECKED_CAST")
        private fun safeParseEvent(doc: DocumentSnapshot): Event? =
            try {
                // Check if eventId field exists in the document (legacy data)
                if (doc.contains("eventId")) {
                    Log.w(TAG, "Document ${doc.id} has legacy eventId field, parsing manually")
                    Event(
                        eventId = doc.id,
                        publisherId = doc.getString("publisherId"),
                        participantIds = doc.get("participantIds") as? List<String>,
                        eventName = doc.getString("eventName"),
                        location = doc.getString("location"),
                        eventDateString = doc.getString("eventDateString"),
                        eventStartTimeString = doc.getString("eventStartTimeString"),
                    )
                } else {
                    // Normal parsing with @DocumentId
                    doc.toObject<Event>()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse document ${doc.id}: ${e.message}", e)
                null
            }

        override suspend fun createDinnerEvent(event: Event): String {
            val currentUserId = auth.currentUserId
            val participantIds = (event.participantIds?.toMutableList() ?: mutableListOf())
            if (currentUserId != null && !participantIds.contains(currentUserId)) {
                participantIds.add(currentUserId)
            }
            val eventWithUserId = event.copy(
                publisherId = currentUserId,
                participantIds = participantIds
            )
            val docRef = Firebase.firestore
                .collection(DINNER_EVENTS_COLLECTION)
                .add(eventWithUserId)
                .await()

            return docRef.id
        }

        override suspend fun readDinnerEvent(noteId: String): Event? {
            return try {
                val doc =
                    Firebase.firestore
                        .collection(DINNER_EVENTS_COLLECTION)
                        .document(noteId)
                        .get()
                        .await()

                if (!doc.exists()) {
                    Log.w(TAG, "Event not found: $noteId")
                    return null
                }

                // Try to parse - if it fails due to eventId field conflict, migrate this document
                try {
                    doc.toObject<Event>()
                } catch (e: RuntimeException) {
                    if (e.message?.contains("@DocumentId") == true) {
                        Log.w(TAG, "Migrating document with eventId field: $noteId")
                        // Remove the eventId field and retry
                        doc.reference
                            .update(
                                "eventId",
                                com.google.firebase.firestore.FieldValue
                                    .delete(),
                            ).await()
                        doc.reference
                            .get()
                            .await()
                            .toObject<Event>()
                    } else {
                        throw e
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading event $noteId: ${e.message}", e)
                null
            }
        }

        override suspend fun updateDinnerEvent(event: Event) {
            val eventId = event.eventId.orEmpty()
            if (eventId.isBlank()) {
                Log.e(TAG, "updateDinnerEvent: eventId is blank, cannot update event")
                throw IllegalArgumentException("Event ID must not be blank when updating an event")
            }
            Log.d(TAG, "updateDinnerEvent: Attempting to update event with id=$eventId")
            Firebase.firestore
                .collection(DINNER_EVENTS_COLLECTION)
                .document(eventId)
                .set(event)
                .await()
            Log.d(TAG, "updateDinnerEvent: Successfully updated event with id=$eventId")
        }

        override suspend fun deleteDinnerEvent(dinnerEventId: String) {
            Firebase.firestore
                .collection(DINNER_EVENTS_COLLECTION)
                .document(dinnerEventId)
                .delete()
                .await()
        }

        /**
         * Migration utility: Removes the 'eventId' field from all documents.
         * This is needed because @DocumentId requires that the field is NOT stored in the document.
         * Call this once to clean up existing data, then remove this function.
         */
        suspend fun migrateRemoveEventIdField() {
            try {
                val querySnapshot =
                    Firebase.firestore
                        .collection(DINNER_EVENTS_COLLECTION)
                        .get()
                        .await()

                var migratedCount = 0
                for (document in querySnapshot.documents) {
                    if (document.contains("eventId")) {
                        document.reference
                            .update(
                                "eventId",
                                com.google.firebase.firestore.FieldValue
                                    .delete(),
                            ).await()
                        migratedCount++
                    }
                }
                Log.d(TAG, "Migration complete: $migratedCount documents updated")
            } catch (e: Exception) {
                Log.e(TAG, "Migration failed: ${e.message}", e)
            }
        }

        override fun observeDinnerEvent(dinnerEventId: String): Flow<Event?> =
            callbackFlow {
                try {
                    val docRef =
                        Firebase.firestore
                            .collection(DINNER_EVENTS_COLLECTION)
                            .document(dinnerEventId)

                    val registration =
                        docRef.addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(
                                    TAG,
                                    "Error observing event $dinnerEventId: ${error.message}",
                                    error
                                )
                                trySend(null)
                                return@addSnapshotListener
                            }

                            if (snapshot == null || !snapshot.exists()) {
                                Log.w(TAG, "Event $dinnerEventId no longer exists")
                                trySend(null)
                                return@addSnapshotListener
                            }

                            val event = safeParseEvent(snapshot)
                            trySend(event)
                        }

                    awaitClose { registration.remove() }
                }
                catch (e: Exception) {
                    Log.e(TAG, "observeDinnerEvent failed: ${e.message}", e)
                    trySend(null)
                }
            }

        override suspend fun deleteDinnerEventsByUser(userId: String) {
            val querySnapshot = Firebase.firestore
                .collection(DINNER_EVENTS_COLLECTION)
                .whereEqualTo(USER_ID_FIELD, userId)
                .get()
                .await()
            for (document in querySnapshot.documents) {
                document.reference.delete().await()
            }
        }

        companion object {
            private const val TAG = "DinnerEventService"
            private const val USER_ID_FIELD = "publisherId"
            private const val DINNER_EVENTS_COLLECTION = "dinnerEvents"
        }
    }
