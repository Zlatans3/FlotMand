package dk.zlatan.flotmand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import dagger.hilt.android.AndroidEntryPoint
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.navigation.FmNavHost
import dk.zlatan.flotmand.ui.FmAppState
import dk.zlatan.flotmand.ui.rememberFmAppState
import dk.zlatan.flotmand.util.NetworkMonitor
import dk.zlatan.flotmand.util.getRandomOfflineMessage
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
            FlotMandTheme {
                val appState = rememberFmAppState(
                    networkMonitor = networkMonitor,
                )

                FlotMandApp(appState)
            }
        }
    }
}

@Composable
fun FlotMandApp(
    appState: FmAppState
) {
    val viewModel: MainActivityViewModel = hiltViewModel()
    val currentDestination = appState.currentDestination
    val loginRoutes = setOf(
        dk.zlatan.flotmand.Features.authentication.login.navigation.LoginGraphRoute,
        dk.zlatan.flotmand.Features.authentication.login.navigation.LoginRoute.toString()
    )
    val showBottomBar = currentDestination?.route !in loginRoutes

    if (showBottomBar) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                appState.topLevelDestinations.forEach {  destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true

                    item(
                        selected = selected,
                        onClick = { appState.navigateToTopLevelDestination(destination) },
                        icon = {
                            Icon(
                                imageVector = destination.unselectedIcon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.iconTextId)) },
                        modifier = Modifier
                            .testTag("FmNavItem")
                        // TODO: Zlatan 25/11/2025 Maybe some day
//                        .then(if (hasUnread) Modifier.notificationDot() else Modifier),
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                val snackbarHostState = remember { SnackbarHostState() }
                val isOffline by appState.isOffline.collectAsStateWithLifecycle()

                // Show snackbar when offline
                LaunchedEffect(isOffline) {
                    if (isOffline) {
                        snackbarHostState.showSnackbar(
                            message = getRandomOfflineMessage(),
                            duration = Indefinite,
                        )
                    }
                }

                FmNavHost(
                    appState = appState,
                    onShowSnackBar = { message, action ->
                        snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = action,
                            duration = SnackbarDuration.Short,
                        ) == ActionPerformed
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    } else {
        // No bottom bar for login
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            val snackbarHostState = remember { SnackbarHostState() }
            val isOffline by appState.isOffline.collectAsStateWithLifecycle()

            // Show snackbar when offline
            LaunchedEffect(isOffline) {
                if (isOffline) {
                    snackbarHostState.showSnackbar(
                        message = getRandomOfflineMessage(),
                        duration = Indefinite,
                    )
                }
            }

            FmNavHost(
                appState = appState,
                onShowSnackBar = { message, action ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = action,
                        duration = SnackbarDuration.Short,
                    ) == ActionPerformed
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}