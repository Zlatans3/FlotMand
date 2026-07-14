package dk.zlatan.flotmand.Features.frontpage

import androidx.annotation.StringRes
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.dialogs.ChangeType

data class WhatsNewEntry(
    val type: ChangeType,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int? = null,
)

/**
 * The changelog shown in [dk.zlatan.flotmand.design_system.componenets.dialogs.FmWhatsNewDialog].
 * The dialog is shown once per [dk.zlatan.flotmand.BuildConfig.VERSION_NAME]. Replace [entries]
 * when releasing an update; leave the list empty to skip the dialog for a release.
 */
object WhatsNewContent {
    val entries =
        listOf(
            WhatsNewEntry(
                type = ChangeType.NEW,
                titleRes = R.string.whats_new_140_images_title,
                descriptionRes = R.string.whats_new_140_images_description,
            ),
            WhatsNewEntry(
                type = ChangeType.IMPROVED,
                titleRes = R.string.whats_new_140_polls_title,
                descriptionRes = R.string.whats_new_140_polls_description,
            ),
            WhatsNewEntry(
                type = ChangeType.IMPROVED,
                titleRes = R.string.whats_new_140_profile_title,
                descriptionRes = R.string.whats_new_140_profile_description,
            ),
            WhatsNewEntry(
                type = ChangeType.FIXED,
                titleRes = R.string.whats_new_140_fixes_title,
                descriptionRes = R.string.whats_new_140_fixes_description,
            ),
        )
}