package dk.zlatan.flotmand.util.feature_flags

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.Preferences

enum class FeatureKey(val prefKey: Preferences.Key<Boolean>) {
    SHOW_PHONE_DIALOG_ON_PRICE_ADDED(
        booleanPreferencesKey("show_phone_dialog_on_price_added"),
    ),
    SHOW_NEXT_HOST_BANNER(
        booleanPreferencesKey("show_next_host_banner"),
    ),
    FORCE_PROFILE_SETUP(
        booleanPreferencesKey("force_profile_setup"),
    ),
}
