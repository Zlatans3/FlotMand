package dk.zlatan.flotmand.Features.profile.theme

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.design_system.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel
    @Inject
    constructor(
        private val themeRepository: ThemeRepository,
    ) : ViewModel() {
        val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode

        fun setThemeMode(mode: ThemeMode) {
            themeRepository.setThemeMode(mode)
        }
    }
