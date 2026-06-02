package dk.zlatan.flotmand.util

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yalantis.ucrop.UCropActivity

/**
 * Thin wrapper around [UCropActivity] that fixes system bar overlap on API 35+.
 *
 * Android 15 (API 35) enforces edge-to-edge for all apps targeting API 35 or higher.
 * The `android:windowOptOutEdgeToEdgeEnforcement` theme attribute only works for apps
 * targeting API 34 or lower, so it cannot be used here. uCrop itself does not handle
 * window insets, causing its toolbar to render behind the status bar and its controls
 * to render behind the navigation bar.
 *
 * The fix: intercept the system bar insets on the DecorView (the root of uCrop's entire
 * view hierarchy) and apply them as padding, then mark them as consumed so uCrop's child
 * views do not try to handle them a second time. This is mechanically equivalent to the
 * old `setDecorFitsSystemWindows(true)` behaviour and works on all API levels.
 *
 * No changes to uCrop's functionality — only inset handling is added.
 */
class CropActivity : UCropActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(window.decorView)
    }
}
