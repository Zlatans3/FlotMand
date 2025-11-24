package dk.zlatan.flotmand.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dk.zlatan.flotmand.Features.authentication.login.LoginScreen
import dk.zlatan.flotmand.Features.frontpage.FrontpageContent
import dk.zlatan.flotmand.Features.profile.ProfileScreen
import androidx.hilt.navigation.compose.hiltViewModel
import dk.zlatan.flotmand.model.DinnerEvent
import dk.zlatan.flotmand.model.User

@Composable
fun AppNavGraph(
    navController: NavHostController,
    user: User?,
    dinnerEvents: List<DinnerEvent>,
    modifier: Modifier = Modifier
) {
    val isLoggedIn = user != null
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            if (!isLoggedIn) {
                LoginScreen(
                    viewModel = hiltViewModel(),
                    modifier = Modifier,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable(Screen.Home.route) {
            if (isLoggedIn) {
                FrontpageContent(modifier = Modifier)
            }
        }
        composable(Screen.AddEvent.route) {
            if (isLoggedIn) {
                Text("Add Event screen coming soon", modifier = Modifier)
            }
        }
        composable(Screen.Profile.route) {
            if (isLoggedIn) {
                ProfileScreen(
                    modifier = Modifier,
                    userImage = null, // Remember to provide user image if available
                    userName = user.displayName
                )
            }
        }
    }
}
