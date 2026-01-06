package dk.zlatan.flotmand.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl @Inject constructor() : AccountService {

    private val _manualUserUpdates = MutableSharedFlow<User?>(replay = 0)

    override val currentUser: Flow<User?>
        get() = merge(
            callbackFlow {
                val listener =
                    FirebaseAuth.AuthStateListener { auth ->
                        this.trySend(auth.currentUser.toNotesUser())
                    }
                Firebase.auth.addAuthStateListener(listener)
                awaitClose { Firebase.auth.removeAuthStateListener(listener) }
            },
            _manualUserUpdates
        )

    override val currentUserId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    override fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    override fun getUserProfile(): User {
        return Firebase.auth.currentUser.toNotesUser()
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            Firebase.firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
                .toObject()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()

        return try {
            // Firestore 'in' query supports up to 10 items
            // If we have more, we need to batch the requests
            val users = mutableListOf<User>()
            userIds.chunked(10).forEach { chunk ->
                val querySnapshot = Firebase.firestore
                    .collection(USERS_COLLECTION)
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                    .get()
                    .await()

                querySnapshot.documents.forEach { document ->
                    document.toObject<User>()?.let { users.add(it) }
                }
            }
            users
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun createAnonymousAccount() {
        Firebase.auth.signInAnonymously().await()
    }

    override suspend fun updateDisplayName(newDisplayName: String) {
        val profileUpdates = userProfileChangeRequest {
            displayName = newDisplayName
        }

        Firebase.auth.currentUser!!.updateProfile(profileUpdates).await()
    }

    override suspend fun updatePhoneNumber(newPhoneNumber: String) {
        // Note: Firebase requires phone verification to update phone numbers
        // This would require implementing PhoneAuthProvider with SMS verification
        // For now, throw an exception indicating the feature needs implementation
        throw UnsupportedOperationException(
            "Opdatering af telefonnummer kræver SMS-verifikation. " +
            "Denne funktion er endnu ikke implementeret."
        )

        // Full implementation would look like:
        // 1. Send SMS code: PhoneAuthProvider.verifyPhoneNumber()
        // 2. User enters code
        // 3. Create credential: PhoneAuthProvider.getCredential(verificationId, code)
        // 4. Update phone: Firebase.auth.currentUser!!.updatePhoneNumber(credential).await()
    }

    override suspend fun linkAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        Firebase.auth.currentUser!!.linkWithCredential(credential).await()
    }

    override suspend fun signIn(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(firebaseCredential).await()
    }

    override suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
    }

    override suspend fun reloadUser() {
        Firebase.auth.currentUser?.reload()?.await()
        // Manually emit the updated user to trigger UI update
        _manualUserUpdates.emit(Firebase.auth.currentUser.toNotesUser())
    }

    private fun FirebaseUser?.toNotesUser(): User {
        return if (this == null) User(
            id = "",
            email = "",
            provider = "",
            phoneNumber = "",
            displayName = "",
            photoUrl = "",
            isAnonymous = true
        ) else User(
            id = this.uid,
            email = this.email ?: "",
            provider = this.providerId,
            phoneNumber = this.phoneNumber ?: "",
            displayName = this.displayName ?: "",
            photoUrl = this.photoUrl?.toString() ?: "",
            isAnonymous = this.isAnonymous
        )
    }

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}