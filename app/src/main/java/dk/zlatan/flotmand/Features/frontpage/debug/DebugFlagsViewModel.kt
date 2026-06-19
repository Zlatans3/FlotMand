package dk.zlatan.flotmand.Features.frontpage.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.util.feature_flags.FeatureKey
import dk.zlatan.flotmand.util.feature_flags.FeatureFlagManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebugFlagItem(
    val key: FeatureKey,
    val displayName: String,
    val description: String,
    val isEnabled: Boolean,
)

@HiltViewModel
class DebugFlagsViewModel @Inject constructor(
    private val featureFlagManager: FeatureFlagManager,
) : ViewModel() {

    val flags: StateFlow<List<DebugFlagItem>> = combine(
        featureFlagManager.isEnabled(FeatureKey.SHOW_PHONE_DIALOG_ON_PRICE_ADDED),
        featureFlagManager.isEnabled(FeatureKey.SHOW_NEXT_HOST_BANNER),
    ) { showPhoneDialog, showNextHostBanner ->
        listOf(
            DebugFlagItem(
                key = FeatureKey.SHOW_PHONE_DIALOG_ON_PRICE_ADDED,
                displayName = "Phone dialog on price added",
                description = "Prompt host to add a phone number when setting a price for the first time",
                isEnabled = showPhoneDialog,
            ),
            DebugFlagItem(
                key = FeatureKey.SHOW_NEXT_HOST_BANNER,
                displayName = "Next host banner",
                description = "Show a banner on the front page when it's the user's next turn to host",
                isEnabled = showNextHostBanner,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun toggle(key: FeatureKey) {
        viewModelScope.launch {
            featureFlagManager.toggle(key)
        }
    }
}
