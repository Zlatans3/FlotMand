package dk.zlatan.flotmand.Features.profile.theme

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.design_system.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val PREFS_NAME = "theme_prefs"
private const val KEY_THEME_MODE = "theme_mode"

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val app: Application,
) : ViewModel() {
    private val prefs: SharedPreferences by lazy {
        app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
    }

    private fun getSavedThemeMode(): ThemeMode =
        runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)
}
