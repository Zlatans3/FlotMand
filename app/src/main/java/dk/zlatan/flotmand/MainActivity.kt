package dk.zlatan.flotmand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.navigation.AppNavigation
import dk.zlatan.flotmand.util.NetworkMonitor
import dk.zlatan.flotmand.util.LocaleContextWrapper
import java.util.Locale
import android.content.Context
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor

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

        setContent {
            FlotMandApp()
        }
    }
}

@Composable
fun FlotMandApp(
    modifier: Modifier = Modifier
) {
    FlotMandTheme(
        // enable when colors are ready
        //     dynamicColor = false
    ) {
        AppNavigation(modifier = modifier)
    }
}
