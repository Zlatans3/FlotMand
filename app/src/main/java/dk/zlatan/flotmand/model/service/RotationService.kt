package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.Group
import dk.zlatan.flotmand.model.RotationMonth
import kotlinx.coroutines.flow.Flow

interface RotationService {
    fun observeGroup(groupId: String): Flow<Group?>
    fun observeTimelineOverrides(groupId: String): Flow<List<RotationMonth>>
    suspend fun releaseMonth(groupId: String, monthId: String, releasedByUserId: String)
    suspend fun claimMonth(groupId: String, monthId: String, claimedByUserId: String)
    suspend fun addUserToRotation(groupId: String, userId: String)
    suspend fun removeUserFromRotation(groupId: String, userId: String)
    suspend fun assignMonthHost(groupId: String, monthId: String, newHostId: String)
    suspend fun createGhostUser(groupId: String, displayName: String)
    suspend fun reorderRotation(groupId: String, newOrder: List<String>)
    suspend fun resetTimelineOverrides(groupId: String)
}
