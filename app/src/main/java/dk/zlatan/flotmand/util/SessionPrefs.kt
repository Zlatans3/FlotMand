package dk.zlatan.flotmand.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "session_prefs"
private const val KEY_NOTIFICATION_PROMPT_SHOWN = "notification_prompt_shown"

/**
 * Flags scoped to the current login session. Cleared by AccountServiceImpl on
 * sign-out / account deletion, so each new login starts fresh.
 */
@Singleton
class SessionPrefs
    @Inject
    constructor(
        app: Application,
    ) {
        private val prefs: SharedPreferences =
            app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        /** Whether the notification-permission prompt has been shown this login. */
        fun isNotificationPromptShown(): Boolean = prefs.getBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, false)

        fun markNotificationPromptShown() {
            prefs.edit { putBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, true) }
        }

        fun clear() {
            prefs.edit { clear() }
        }
    }
