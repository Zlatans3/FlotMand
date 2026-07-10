package dk.zlatan.flotmand.design_system.componenets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmConfirmDialog
import dk.zlatan.flotmand.util.SessionPrefs
import javax.inject.Inject

@HiltViewModel
class NotificationPermissionViewModel
    @Inject
    constructor(
        private val sessionPrefs: SessionPrefs,
    ) : ViewModel() {
        fun shouldPrompt(): Boolean = !sessionPrefs.isNotificationPromptShown()

        fun markPrompted() = sessionPrefs.markNotificationPromptShown()
    }

/**
 * Handles the POST_NOTIFICATIONS permission flow on Android 13+.
 *
 * Drop this anywhere in the post-login composition tree — it renders no UI of its own,
 * only showing dialogs when necessary. The rationale is shown at most once per login:
 * the flag persists across app restarts (SessionPrefs) and is cleared on sign-out,
 * so a user who declined isn't nagged on every launch.
 *
 * Flow:
 *   already granted → silent no-op
 *   not granted     → rationale dialog → system prompt
 *   permanently denied (after prompt) → "go to Settings" dialog
 */
@Composable
fun NotificationPermissionHandler(viewModel: NotificationPermissionViewModel = hiltViewModel()) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current

    val isGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED

    var showRationale by rememberSaveable { mutableStateOf(false) }
    var showGoToSettings by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            // shouldShowRequestPermissionRationale returns false after permanent denial
            val activity = context as? android.app.Activity
            val permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.POST_NOTIFICATIONS,
                )
            if (permanentlyDenied) showGoToSettings = true
        }
    }

    // Show rationale once per login if permission has not been granted yet
    LaunchedEffect(Unit) {
        if (!isGranted && viewModel.shouldPrompt()) {
            viewModel.markPrompted()
            showRationale = true
        }
    }

    if (showRationale) {
        FmConfirmDialog(
            title = context.getString(R.string.notification_permission_title),
            message = context.getString(R.string.notification_permission_rationale),
            confirmText = context.getString(R.string.notification_permission_allow),
            dismissText = context.getString(R.string.notification_permission_not_now),
            onDismiss = { showRationale = false },
            onConfirmClick = {
                showRationale = false
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
        )
    }

    if (showGoToSettings) {
        FmConfirmDialog(
            title = context.getString(R.string.notification_permission_blocked_title),
            message = context.getString(R.string.notification_permission_blocked_message),
            confirmText = context.getString(R.string.notification_permission_open_settings),
            dismissText = context.getString(R.string.notification_permission_not_now),
            onDismiss = { showGoToSettings = false },
            onConfirmClick = {
                showGoToSettings = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
        )
    }
}
