package dk.zlatan.flotmand

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationCoordinator
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.impl.FlotMandFirebaseMessagingService
import dk.zlatan.flotmand.navigation.AppNavigation
import dk.zlatan.flotmand.util.LocaleContextWrapper
import dk.zlatan.flotmand.util.NetworkMonitor
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var frontPageNavigationCoordinator: FrontPageNavigationCoordinator

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("selected_language", "da") ?: "da"
        val locale = Locale(lang)
        val context = LocaleContextWrapper.wrap(newBase, locale)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            )
        )
        // Only handle on a true cold start — savedInstanceState is non-null on rotation.
        if (savedInstanceState == null) {
            handleNotificationIntent(intent)
        }
        setContent {
            FlotMandApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(FlotMandFirebaseMessagingService.EXTRA_OPEN_NOTIFICATIONS, false) == true) {
            frontPageNavigationCoordinator.navigate(FrontPageDestination.Notifications)
        }
    }
}

@Composable
fun FlotMandApp(
    modifier: Modifier = Modifier
) {
    FlotMandTheme {
        AppNavigation(modifier = modifier)
    }
}
