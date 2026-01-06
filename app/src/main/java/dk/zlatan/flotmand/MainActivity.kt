package dk.zlatan.flotmand

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dk.zlatan.flotmand.Features.authentication.login.LoginRoute
import dk.zlatan.flotmand.Features.authentication.login.LoginScreen
import dk.zlatan.flotmand.Features.bottomnavigation.FmBottomNavigationBar
import dk.zlatan.flotmand.Features.bottomnavigation.TopLevelDestination
import dk.zlatan.flotmand.Features.frontpage.di.FrontPageNavigation
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigation
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigation
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.util.NetworkMonitor
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor

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
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentTab: TopLevelDestination by rememberSaveable {
        mutableStateOf(TopLevelDestination.HOME)
    }

    FlotMandTheme {
        // Show login screen if user is not logged in
        if (!uiState.isLoggedIn) {
            LoginRoute()
        } else {
            // Show main app content when user is logged in
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    FmBottomNavigationBar(
                        currentTab = currentTab,
                        selectedTabIconColor = MaterialTheme.colorScheme.primary,
                        onBottomNavigationClicked = { destination ->
                            currentTab = destination
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        TopLevelDestination.HOME -> {
                            FrontPageNavigation()
                        }
                        TopLevelDestination.MY_EVENTS -> {
                            MyEventsNavigation()
                        }
                        TopLevelDestination.PROFILE -> {
                            ProfileNavigation()
                        }
                    }
                }
            }

            val window = LocalActivity.current?.window
            LaunchedEffect(currentTab) {
                when (currentTab) {
                    TopLevelDestination.HOME,
                    TopLevelDestination.PROFILE -> {
                        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    else -> window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }
}
