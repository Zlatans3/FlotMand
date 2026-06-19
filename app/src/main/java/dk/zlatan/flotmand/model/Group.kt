package dk.zlatan.flotmand.model

import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val rotationOrder: List<String> = emptyList(),
    val anchorMonth: String = "",   // "YYYY-MM" — the reference month for rotation calculation
    val anchorIndex: Int = 0,       // index in rotationOrder hosting that anchor month
    val timezone: String = "Europe/Copenhagen",
    val createdAt: String = "",
) {
    companion object {
        val mock = Group(
            id = "group_flotmand",
            name = "FlotMand",
            members = listOf("uid_david", "uid_lasse", "uid_zlatan", "uid_oliver", "uid_mikkel", "uid_gustav"),
            rotationOrder = listOf("uid_david", "uid_lasse", "uid_zlatan", "uid_oliver", "uid_mikkel", "uid_gustav"),
            anchorMonth = "2026-06",
            anchorIndex = 0,
            timezone = "Europe/Copenhagen",
            createdAt = "2026-01-01",
        )
    }
}
