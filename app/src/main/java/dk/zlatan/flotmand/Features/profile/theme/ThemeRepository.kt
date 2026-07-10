package dk.zlatan.flotmand.Features.profile.theme

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dk.zlatan.flotmand.design_system.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "theme_prefs"
private const val KEY_THEME_MODE = "theme_mode"

@Singleton
class ThemeRepository
    @Inject
    constructor(
        app: Application,
    ) {
        private val prefs: SharedPreferences =
            app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        private val _themeMode = MutableStateFlow(getSavedThemeMode())
        val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

        fun setThemeMode(mode: ThemeMode) {
            prefs.edit { putString(KEY_THEME_MODE, mode.name) }
            _themeMode.value = mode
        }

        private fun getSavedThemeMode(): ThemeMode =
            runCatching {
                ThemeMode.valueOf(
                    prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name,
                )
            }.getOrDefault(ThemeMode.SYSTEM)
                // FLOTMAND is not selectable in theme settings; mapping it to SYSTEM
                // guarantees the settings screen always has a selected option.
                .let { if (it == ThemeMode.FLOTMAND) ThemeMode.SYSTEM else it }
    }
