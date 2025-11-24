package dk.zlatan.flotmand.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import dk.zlatan.flotmand.Features.authentication.login.LoginScreen
import dk.zlatan.flotmand.Features.frontpage.FrontpageContent
import dk.zlatan.flotmand.Features.profile.ProfileScreen
import dk.zlatan.flotmand.model.DinnerEvent
import dk.zlatan.flotmand.model.User

object AppNav {
    const val HOME_ROUTE = "home"
    const val LOGIN_ROUTE = "login"
    const val ADD_EVENT_ROUTE = "add_event"
    const val PROFILE_ROUTE = "profile"

    fun NavGraphBuilder.homeScreen(
        user: User?,
        dinnerEvents: List<DinnerEvent>
    ) {
        composable(HOME_ROUTE) {
            if (user != null) {
                FrontpageContent(modifier = Modifier)
            }
        }
    }

    fun NavGraphBuilder.loginScreen() {
        composable(LOGIN_ROUTE) {
            LoginScreen()
        }
    }

    fun NavGraphBuilder.addEventScreen(user: User?) {
        composable(ADD_EVENT_ROUTE) {
            if (user != null) {
                Text("Add Event screen coming soon", modifier = Modifier)
            }
        }
    }

    fun NavGraphBuilder.profileScreen(user: User?) {
        composable(PROFILE_ROUTE) {
            if (user != null) {
                ProfileScreen(
                    modifier = Modifier,
                    userImage = null,
                    userName = user.displayName
                )
            }
        }
    }

    fun NavHostController.navigateToHome(navOptions: NavOptions? = null) {
        this.navigate(HOME_ROUTE, navOptions)
    }
    fun NavHostController.navigateToLogin(navOptions: NavOptions? = null) {
        this.navigate(LOGIN_ROUTE, navOptions)
    }
    fun NavHostController.navigateToAddEvent(navOptions: NavOptions? = null) {
        this.navigate(ADD_EVENT_ROUTE, navOptions)
    }
    fun NavHostController.navigateToProfile(navOptions: NavOptions? = null) {
        this.navigate(PROFILE_ROUTE, navOptions)
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    user: User?,
    isAuthChecked: Boolean,
    dinnerEvents: List<DinnerEvent>,
    modifier: Modifier = Modifier
) {
    if (!isAuthChecked) {
        LoadingScreen()
        return
    }
    val startDestination = if (user != null) AppNav.HOME_ROUTE else AppNav.LOGIN_ROUTE
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        with(AppNav) {
            loginScreen()
            homeScreen(user, dinnerEvents)
            addEventScreen(user)
            profileScreen(user)
        }
    }
}
