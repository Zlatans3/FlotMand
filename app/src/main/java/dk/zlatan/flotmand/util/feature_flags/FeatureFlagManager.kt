package dk.zlatan.flotmand.util.feature_flags

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dk.zlatan.flotmand.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    fun isEnabled(key: FeatureKey): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[key.prefKey] ?: false }

    suspend fun toggle(key: FeatureKey) {
        check(BuildConfig.DEBUG) { "Feature flags can only be toggled in debug builds" }
        dataStore.edit { prefs ->
            prefs[key.prefKey] = !(prefs[key.prefKey] ?: false)
        }
    }

    // No debug check: disabling is a no-op in release builds, where flags are never set,
    // and app flows use it to reset one-shot flags (e.g. FORCE_PROFILE_SETUP).
    suspend fun disable(key: FeatureKey) {
        dataStore.edit { prefs ->
            prefs[key.prefKey] = false
        }
    }
}
