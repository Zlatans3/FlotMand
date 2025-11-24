package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.DinnerEvent
import kotlinx.coroutines.flow.Flow


interface DinnerEventService {
    val dinnerEvents: Flow<List<DinnerEvent>>
    suspend fun createDinnerEvent(dinnerEvent: DinnerEvent)
    suspend fun readDinnerEvent(dinnerEventId: String): DinnerEvent?
    suspend fun updateDinnerEvent(dinnerEvent: DinnerEvent)
    suspend fun deleteDinnerEvent(dinnerEventId: String)
}