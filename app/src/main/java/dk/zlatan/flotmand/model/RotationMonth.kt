package dk.zlatan.flotmand.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class RotationMonth(
    @DocumentId val monthId: String = "",   // "YYYY-MM", also the Firestore document ID
    val status: String = RotationMonth.Status.VACANT,
    val releasedBy: String = "",
    val overrideHostId: String? = null,     // set only when status == CLAIMED
    val claimedAt: String? = null,
    val updatedAt: String = "",
) {
    @get:Exclude
    val isVacant: Boolean get() = status == Status.VACANT

    @get:Exclude
    val isClaimed: Boolean get() = status == Status.CLAIMED

    object Status {
        const val VACANT = "vacant"
        const val CLAIMED = "claimed"
    }

    companion object {
        fun vacantMonth(monthId: String, releasedBy: String, updatedAt: String) = RotationMonth(
            monthId = monthId,
            status = Status.VACANT,
            releasedBy = releasedBy,
            updatedAt = updatedAt,
        )

        fun claimedMonth(monthId: String, releasedBy: String, claimedBy: String, claimedAt: String) = RotationMonth(
            monthId = monthId,
            status = Status.CLAIMED,
            releasedBy = releasedBy,
            overrideHostId = claimedBy,
            claimedAt = claimedAt,
            updatedAt = claimedAt,
        )
    }
}
