package dk.zlatan.flotmand.util

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yalantis.ucrop.UCropActivity

/**
 * Fixes a bug in the uCrop that was causing the activity to not apply the correct insets
 *
 * For reference, we use Ucrop for cropping user profile pictures
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
