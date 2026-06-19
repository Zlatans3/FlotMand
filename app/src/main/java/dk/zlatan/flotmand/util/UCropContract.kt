package dk.zlatan.flotmand.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.yalantis.ucrop.UCrop

/**
 * ActivityResultContract that launches uCrop for profile photo cropping.
 *
 * Input:  Pair(sourceUri, destinationUri)
 *   - sourceUri      — the image selected by the system photo picker
 *   - destinationUri — a temp file in cacheDir where the cropped result is written
 * Output: the cropped image Uri, or null if the user cancelled or an error occurred.
 *
 * Configuration choices:
 *   - 1:1 aspect ratio enforced — guarantees the result always fits the circular ProfileImage frame
 *   - 512×512 max output size   — sufficient for a profile picture at any screen density
 *   - Circle overlay            — previews exactly how the image will look in the UI
 *   - Grid and frame hidden     — not needed alongside the circle overlay
 *
 * The intent is redirected to [CropActivity] rather than UCropActivity directly so that
 * system bar insets are handled correctly on API 35+ (see [CropActivity] for details).
 */
class UCropContract : ActivityResultContract<Pair<Uri, Uri>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<Uri, Uri>): Intent {
        val (source, destination) = input
        return UCrop.of(source, destination)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)
            .withOptions(
                UCrop.Options().apply {
                    setCircleDimmedLayer(true)
                    setShowCropGrid(false)
                    setShowCropFrame(false)
                }
            )
            .getIntent(context)
            .apply { setClass(context, CropActivity::class.java) }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null
        return UCrop.getOutput(intent)
    }
}
