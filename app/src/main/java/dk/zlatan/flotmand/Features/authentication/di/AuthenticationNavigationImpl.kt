package dk.zlatan.flotmand.Features.authentication.di

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.authentication.login.LoginRoute
import dk.zlatan.flotmand.Features.authentication.navigation.AuthenticationDestination
import dk.zlatan.flotmand.Features.authentication.navigation.AuthenticationNavigationViewModel

@Composable
fun AuthenticationNavigation(
    viewModel: AuthenticationNavigationViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    val navigationStack: List<AuthenticationDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key ->
            when (key) {
                AuthenticationDestination.Login -> NavEntry(key) {
                    LoginRoute(
                        modifier = Modifier.fillMaxSize(),
                        onLoginSuccess = { onLoginSuccess() }
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
            rememberSaveableStateHolderNavEntryDecorator()
        )
    )
}


