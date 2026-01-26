package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.Event
import kotlinx.coroutines.flow.Flow


interface DinnerEventService {
    // All events from all users
    val allDinnerEvents: Flow<List<Event>>

    // Current user's events only
    val dinnerEventsByUserId: Flow<List<Event>>

    // Realtime listener for a specific event document
    fun observeDinnerEvent(dinnerEventId: String): Flow<Event?>

    suspend fun createDinnerEvent(dinnerEvent: Event): String
    suspend fun readDinnerEvent(dinnerEventId: String): Event?
    suspend fun updateDinnerEvent(dinnerEvent: Event)
    suspend fun deleteDinnerEvent(dinnerEventId: String)
}