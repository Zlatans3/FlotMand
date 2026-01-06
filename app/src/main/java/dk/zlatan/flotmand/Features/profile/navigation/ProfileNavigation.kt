package dk.zlatan.flotmand.Features.profile.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.profile.ProfileScreenRoute
import dk.zlatan.flotmand.Features.profile.account_information.AccountInformationScreenRoute

@Suppress("CyclomaticComplexMethod")
@Composable
fun ProfileNavigation(viewModel: ProfileNavigationViewModel = hiltViewModel()) {
    val navigationStack: List<ProfileDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()

    // Navigation overlay for sub-screens
    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key ->
            when (key) {
                ProfileDestination.ProfileScreen -> NavEntry(key) {
                    ProfileScreenRoute(
                        navigateToLogin = {
                            // Navigation to login handled by app-level navigation
                            // When user logs out, the auth state change will trigger navigation
                        },
                        navigateToAccountInformation = {
                            viewModel.navigate(ProfileDestination.AccountInformation)
                        }
                    )
                }

                ProfileDestination.AccountInformation -> NavEntry(key) {
                    AccountInformationScreenRoute(
                        onDismiss = { viewModel.pop() }
                    )
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
                ),
                ExitTransition.None
            )
        },
        popTransitionSpec = {
            ContentTransform(
                EnterTransition.None,
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right
                )
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
//            rememberViewModelStoreNavEntryDecorator()
        )
    )
}
