package dk.zlatan.flotmand.Features.profile.switchlanguage

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.content.edit

private const val PREFS_NAME = "language_prefs"
private const val KEY_SELECTED_LANGUAGE = "selected_language"
private const val DEFAULT_LANGUAGE = "da"

data class SwitchLanguageUiState(
    val selectedLanguage: String = "da",
    val loading: Boolean = false,
)

@HiltViewModel
class SwitchLanguageViewModel
    @Inject
    constructor(
        private val app: Application,
    ) : ViewModel() {
        private val prefs: SharedPreferences by lazy {
            app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        private val _uiState =
            MutableStateFlow(
                SwitchLanguageUiState(
                    selectedLanguage = getSavedLanguage(),
                    loading = false,
                ),
            )
        val uiState: StateFlow<SwitchLanguageUiState> = _uiState.asStateFlow()

        fun onLanguageSelected(languageCode: String) {
            if (_uiState.value.selectedLanguage != languageCode) {
                viewModelScope.launch {
                    // Simulate loading state because changing locale might take some time
                    // and we want to show feedback to the user
                    _uiState.update { it.copy(loading = true, selectedLanguage = languageCode) }
                    saveLanguage(languageCode)
                    delay(100)
                    _uiState.update { it.copy(loading = false, selectedLanguage = languageCode) }
                }
            }
        }

        private fun saveLanguage(languageCode: String) {
            prefs.edit { putString(KEY_SELECTED_LANGUAGE, languageCode) }
        }

        private fun getSavedLanguage(): String = prefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
