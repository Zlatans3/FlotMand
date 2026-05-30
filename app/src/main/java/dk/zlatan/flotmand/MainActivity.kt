package dk.zlatan.flotmand

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationCoordinator
import dk.zlatan.flotmand.Features.profile.theme.ThemeViewModel
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.design_system.theme.ThemeMode
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

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("language_prefs", MODE_PRIVATE)
        val lang = prefs.getString("selected_language", "da") ?: "da"
        val locale = Locale.forLanguageTag(lang)
        val context = LocaleContextWrapper.wrap(newBase, locale)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.auto(
                    lightScrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT,
                ),
        )
        // Only handle on a true cold start — savedInstanceState is non-null on rotation.
        if (savedInstanceState == null) {
            handleNotificationIntent(intent)
        }
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM, ThemeMode.FLOTMAND -> systemDark
            }
            val dynamicColor = themeMode == ThemeMode.SYSTEM
            FlotMandTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                AppNavigation(modifier = Modifier)
            }
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
