package dk.zlatan.flotmand.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object AddEvent : Screen("add_event", "Tilføj Event", Icons.Filled.Add)
    object Profile : Screen("profile", "Profile", Icons.Filled.AccountBox)
    object Login : Screen("login", "Login", Icons.Filled.AccountBox)
}
