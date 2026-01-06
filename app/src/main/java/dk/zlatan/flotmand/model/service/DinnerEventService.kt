package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.Event
import kotlinx.coroutines.flow.Flow


interface DinnerEventService {
    val allDinnerEvents: Flow<List<Event>> // All events from all users
    val dinnerEventsByUserId: Flow<List<Event>> // Current user's events only
    suspend fun createDinnerEvent(dinnerEvent: Event)
    suspend fun readDinnerEvent(dinnerEventId: String): Event?
    suspend fun updateDinnerEvent(dinnerEvent: Event)
    suspend fun deleteDinnerEvent(dinnerEventId: String)
}