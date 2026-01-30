package dk.zlatan.flotmand.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import dk.zlatan.flotmand.BuildConfig
import kotlinx.coroutines.tasks.await
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * GoogleAuthUiClient provides Google-only authentication for the app.
 * No other authentication methods are supported or implemented.
 *
 * Usage:
 *   - Only Google account sign-in is available.
 *   - If you wish to add other authentication methods, update this class and all related documentation.
 *
 * Firebase Integration:
 *   - This class is set up to work with Firebase Authentication.
 *   - Make sure your google-services.json is present and configured.
 *   - The Google Server Client ID must match the one in your Firebase project.
 */
class GoogleAuthUiClient(
    private val context: Context,
    private val credentialManager: CredentialManager
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Initiates Google sign-in. No other sign-in methods are available.
     * @return GetCredentialResponse? The Google sign-in response, or null on failure.
     */
    suspend fun signIn(): GetCredentialResponse? {
        val request = buildSignInRequest()
        return try {
            credentialManager.getCredential(
                context = context,
                request = request
            )
        } catch (e: GetCredentialException) {
            Log.d("GoogleAuthUiClient", "signIn: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Builds a Google-only sign-in request. No other credential options are added.
     * Uses the Google Server Client ID from BuildConfig, which must match your Firebase project.
     */
    fun buildSignInRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    /**
     * Use this method to sign in to Firebase with the Google ID token.
     * Call this after a successful Google sign-in.
     * @param idToken The Google ID token from the sign-in response.
     * @return true if sign-in to Firebase was successful, false otherwise.
     */
    suspend fun firebaseSignInWithGoogle(idToken: String): Boolean {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        return try {
            auth.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.d("GoogleAuthUiClient", "firebaseSignInWithGoogle: ${e.localizedMessage}")
            false
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object GoogleAuthUiClientModule {
    @Provides
    @Singleton
    fun provideGoogleAuthUiClient(
        @ApplicationContext context: Context
    ): dk.zlatan.flotmand.util.GoogleAuthUiClient {
        return dk.zlatan.flotmand.util.GoogleAuthUiClient(
            context = context,
            credentialManager = CredentialManager.create(context)
        )
    }
}
