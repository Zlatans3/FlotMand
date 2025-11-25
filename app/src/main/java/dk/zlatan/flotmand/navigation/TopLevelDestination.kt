package dk.zlatan.flotmand.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.icon.FmIcons

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    val route: String,
    val baseRoute: String = route,
) {
    HOME(
        selectedIcon = FmIcons.homeBorder,
        unselectedIcon = FmIcons.Home,
        iconTextId = R.string.home_title,
        baseRoute = "front_page",
        route = "front_page",
    ),
    MY_EVENTS(
        selectedIcon = FmIcons.CalendarBorder,
        unselectedIcon = FmIcons.Calendar,
        iconTextId = R.string.my_event_title,
        route = "my_events",
    ),
    PROFILE(
        selectedIcon = FmIcons.PersonBorder,
        unselectedIcon = FmIcons.Person,
        iconTextId = R.string.profile_title,
        route = "profile",
    ),
}
