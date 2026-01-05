package dk.zlatan.flotmand.Features.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dk.zlatan.flotmand.Features.profile.account_information.AccountInformationScreenRoute

const val AccountInformationRoute = "account_information"

fun NavController.navigateToAccountInformation() {
    navigate(AccountInformationRoute)
}

fun NavGraphBuilder.accountInformationScreen(onDismiss: () -> Unit) {
    composable(route = AccountInformationRoute) {
        AccountInformationScreenRoute(
            onDismiss = onDismiss
        )
    }
}
