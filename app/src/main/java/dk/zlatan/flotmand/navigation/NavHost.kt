package dk.zlatan.flotmand.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import dk.zlatan.flotmand.Features.frontpage.dinner_event.navigaiton.myEventScreen
import dk.zlatan.flotmand.Features.frontpage.dinner_event.navigaiton.navigateToMyEvents
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.frontPageSection
import dk.zlatan.flotmand.Features.profile.navigation.profileScreen
import dk.zlatan.flotmand.ui.FmAppState

@Composable
fun FmNavHost(
    appState: FmAppState,
    onShowSnackBar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = "front_page_graph",
        modifier = modifier,
    ) {
        frontPageSection(
            onEventClicked = { navController.navigateToMyEvents() }
        ) {
            myEventScreen(
                showBackButton = true,
                onBackClick = navController::popBackStack,
                onMyEventClick = { navController.navigateToMyEvents() },
            )
        }
        profileScreen(
            onNavigateBack = navController::popBackStack,
            onSignOut = { }
        )
    }
}