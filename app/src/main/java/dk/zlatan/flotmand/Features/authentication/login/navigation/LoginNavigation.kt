package dk.zlatan.flotmand.Features.authentication.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dk.zlatan.flotmand.Features.authentication.login.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute
const val LoginGraphRoute = "login_graph"

fun NavController.navigateToLogin(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(LoginGraphRoute, builder)
}

fun NavGraphBuilder.loginSection(
    onLoginSuccess: () -> Unit
) {
    navigation(startDestination = LoginRoute.toString(), route = LoginGraphRoute) {
        composable(LoginRoute.toString()) {
            LoginScreen(
                onLoginSuccess = onLoginSuccess
            )
        }
    }
}
