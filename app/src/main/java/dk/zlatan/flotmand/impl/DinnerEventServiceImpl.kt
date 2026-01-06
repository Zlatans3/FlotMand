package dk.zlatan.flotmand.impl

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Firebase
import dk.zlatan.flotmand.model.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import jakarta.inject.Inject

class DinnerEventServiceImpl @Inject constructor(private val auth: AccountService) : DinnerEventService {

    // Fetch all dinner events
    override val allDinnerEvents: Flow<List<Event>>
        get() = callbackFlow {
            Log.d(TAG, "Starting to listen for events")
            val listenerRegistration = Firebase.firestore
                .collection(DINNER_EVENTS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching events", error)
                        close(error) // Close the flow with error instead of sending empty list
                        return@addSnapshotListener
                    }

                    Log.d(TAG, "Snapshot received with ${snapshot?.documents?.size ?: 0} documents")
                    val events = snapshot?.documents?.mapNotNull { doc ->
                        Log.d(TAG, "Processing document: ${doc.id}, exists: ${doc.exists()}")
                        val event = safeParseEvent(doc)
                        if (event == null) {
                            Log.w(TAG, "Failed to parse document ${doc.id}")
                        } else {
                            Log.d(TAG, "Successfully parsed event: ${event.eventName}")
                        }
                        event
                    } ?: emptyList()

                    Log.d(TAG, "Sending ${events.size} events to flow")
                    trySend(events)
                }

            awaitClose {
                Log.d(TAG, "Closing events listener")
                listenerRegistration.remove()
            }
        }

    // Fetch only current user's dinner events (for my events page)
    @OptIn(ExperimentalCoroutinesApi::class)
    override val dinnerEventsByUserId: Flow<List<Event>>
        get() = auth.currentUser.flatMapLatest { user ->
            callbackFlow {
                val listenerRegistration = Firebase.firestore
                    .collection(DINNER_EVENTS_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, user?.id)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Error fetching user events", error)
                            close(error) // Close the flow with error instead of sending empty list
                            return@addSnapshotListener
                        }

                        val events = snapshot?.documents?.mapNotNull { doc ->
                            safeParseEvent(doc)
                        } ?: emptyList()

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
    private fun safeParseEvent(doc: DocumentSnapshot): Event? {
        return try {
            Log.d(TAG, "Parsing document ${doc.id}, data: ${doc.data}")

            // Check if eventId field exists in the document
            if (doc.contains("eventId")) {
                Log.w(TAG, "Document ${doc.id} has eventId field, parsing manually")
                // Parse manually to avoid @DocumentId conflict
                val event = Event(
                    eventId = doc.id, // Use document ID, not the field
                    publisherId = doc.getString("publisherId"),
                    participantIds = doc.get("participantIds") as? List<String>,
                    eventName = doc.getString("eventName"),
                    location = doc.getString("location"),
                    eventDateString = doc.getString("eventDateString"),
                    eventStartTimeString = doc.getString("eventStartTimeString")
                )
                Log.d(TAG, "Manually parsed event: ${event.eventName}, date: ${event.eventDateString}")
                event
            } else {
                // Normal parsing with @DocumentId
                Log.d(TAG, "Parsing with toObject for document ${doc.id}")
                val event = doc.toObject<Event>()
                Log.d(TAG, "toObject parsed event: ${event?.eventName}, eventId: ${event?.eventId}")
                event
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing event from document ${doc.id}: ${e.message}", e)
            null
        }
    }

    override suspend fun createDinnerEvent(event: Event) {
        val eventWithUserId = event.copy(publisherId = auth.currentUserId)
        Firebase.firestore
            .collection(DINNER_EVENTS_COLLECTION)
            .add(eventWithUserId).await()
        // Note: eventId is not stored in the document, it's auto-populated via @DocumentId annotation
    }

    override suspend fun readDinnerEvent(noteId: String): Event? {
        Log.d(TAG, "Fetching event with ID: $noteId")
        return try {
            val doc = Firebase.firestore
                .collection(DINNER_EVENTS_COLLECTION)
                .document(noteId).get().await()

            Log.d(TAG, "Document exists: ${doc.exists()}")
            if (!doc.exists()) return null

            // Try to parse - if it fails due to eventId field conflict, migrate this document
            try {
                val event = doc.toObject<Event>()
                Log.d(TAG, "Parsed event: ${event?.eventName}, eventId: ${event?.eventId}")
                event
            } catch (e: RuntimeException) {
                if (e.message?.contains("@DocumentId") == true) {
                    Log.w(TAG, "Document has eventId field, migrating document: $noteId")
                    // Remove the eventId field from this document
                    doc.reference.update("eventId", com.google.firebase.firestore.FieldValue.delete()).await()
                    // Fetch again and parse
                    val updatedDoc = doc.reference.get().await()
                    val event = updatedDoc.toObject<Event>()
                    Log.d(TAG, "Migrated and parsed event: ${event?.eventName}, eventId: ${event?.eventId}")
                    event
                } else {
                    throw e
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading event: ${e.message}", e)
            null
        }
    }

    override suspend fun updateDinnerEvent(event: Event) {
        Firebase.firestore
            .collection(DINNER_EVENTS_COLLECTION)
            // TODO: Zlatan 27/11/2025 Probably wrong .orEmpty()
            .document(event.eventId.orEmpty()).set(event).await()
    }

    override suspend fun deleteDinnerEvent(dinnerEventId: String) {
        Firebase.firestore
            .collection(DINNER_EVENTS_COLLECTION)
            .document(dinnerEventId).delete().await()
    }

    /**
     * Migration utility: Removes the 'eventId' field from all documents.
     * This is needed because @DocumentId requires that the field is NOT stored in the document.
     * Call this once to clean up existing data, then remove this function.
     */
    suspend fun migrateRemoveEventIdField() {
        try {
            Log.d(TAG, "Starting migration to remove eventId field from documents")
            val querySnapshot = Firebase.firestore
                .collection(DINNER_EVENTS_COLLECTION)
                .get()
                .await()

            var migratedCount = 0
            for (document in querySnapshot.documents) {
                if (document.contains("eventId")) {
                    // Use FieldValue.delete() to remove the field
                    document.reference.update("eventId", com.google.firebase.firestore.FieldValue.delete()).await()
                    migratedCount++
                    Log.d(TAG, "Removed eventId field from document: ${document.id}")
                }
            }
            Log.d(TAG, "Migration complete. Migrated $migratedCount documents")
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "DinnerEventService"
        private const val USER_ID_FIELD = "publisherId"
        private const val DINNER_EVENTS_COLLECTION = "dinnerEvents"
    }
}