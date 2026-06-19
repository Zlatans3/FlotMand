package dk.zlatan.flotmand.Features.frontpage.event_rotation

sealed class RotationTimelineItem {
    abstract val monthId: String      // "YYYY-MM"
    abstract val monthLabel: String   // "Juni", "Juli", etc.
    abstract val isCurrent: Boolean

    data class Normal(
        override val monthId: String,
        override val monthLabel: String,
        override val isCurrent: Boolean,
        val hostId: String,
        val hostName: String,
        val hostPhotoUrl: String?,
    ) : RotationTimelineItem()

    data class Vacant(
        override val monthId: String,
        override val monthLabel: String,
        override val isCurrent: Boolean,
    ) : RotationTimelineItem()
}
