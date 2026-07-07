package dk.zlatan.flotmand.model.service

import android.net.Uri
import dk.zlatan.flotmand.model.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String

    fun hasUser(): Boolean

    fun getUserProfile(): User

    suspend fun getUserById(userId: String): User?

    fun observeUserById(userId: String): Flow<User?>

    suspend fun getUsersByIds(userIds: List<String>): List<User>

    suspend fun createAnonymousAccount()

    suspend fun updateDisplayName(newDisplayName: String)

    suspend fun updatePhoneNumber(newPhoneNumber: String)

    /** Persists the user's bio (trimmed to [User.BIO_MAX_LENGTH] characters). */
    suspend fun updateBio(newBio: String)

    suspend fun updateProfilePhoto(imageUri: Uri)

    suspend fun linkAccount(
        email: String,
        password: String,
    )

    suspend fun signInWithGoogle(idToken: String)

    suspend fun signIn(
        email: String,
        password: String,
    )

    suspend fun signOut()

    suspend fun deleteAccount()

    // had troubles with the user not updating after display name change
    // so added this method to force reload the user and trigger the auth state listener
    suspend fun reloadUser()

    /** Persists [token] to Firestore so Cloud Functions can address this device. */
    suspend fun updateFcmToken(token: String)
}
