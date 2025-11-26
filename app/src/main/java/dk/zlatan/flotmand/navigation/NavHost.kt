package dk.zlatan.flotmand.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.navigation.EventDetailRoute
import dk.zlatan.flotmand.Features.frontpage.navigation.frontPageSection
import dk.zlatan.flotmand.Features.my_events.navigaiton.myEventScreen
import dk.zlatan.flotmand.Features.profile.navigation.profileScreen
import dk.zlatan.flotmand.ui.FmAppState
import dk.zlatan.flotmand.Features.authentication.login.navigation.loginSection
import dk.zlatan.flotmand.Features.authentication.login.navigation.LoginGraphRoute
import dk.zlatan.flotmand.Features.authentication.login.navigation.LoginRoute

@Composable
fun FmNavHost(
    appState: FmAppState,
    onShowSnackBar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = LoginGraphRoute, // Set login as start destination
        modifier = modifier,
    ) {
        loginSection(
            onLoginSuccess = {
                navController.navigate("front_page_graph") {
                    popUpTo(LoginGraphRoute) { inclusive = true }
                }
            }
        )

        frontPageSection(
            onEventClicked = {
                navController.navigate("$EventDetailRoute/$it")
            }
        )

        myEventScreen()

        profileScreen(onNavigateToLogin = {
            navController.navigate(LoginRoute.toString()) {
                popUpTo(0) { inclusive = true }
            }
        })
    }
}