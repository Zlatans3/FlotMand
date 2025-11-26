package dk.zlatan.flotmand.model

data class User(
    val id: String = "",
    val email: String = "",
    val provider: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isAnonymous: Boolean = true
)