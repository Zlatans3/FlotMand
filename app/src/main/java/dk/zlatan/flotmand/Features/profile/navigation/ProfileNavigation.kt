package dk.zlatan.flotmand.Features.profile.navigation

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.profile.ProfileScreenRoute
import dk.zlatan.flotmand.Features.profile.account_information.AccountInformationScreenRoute
import dk.zlatan.flotmand.Features.profile.licenses.LicensesScreen
import dk.zlatan.flotmand.Features.profile.notificationsettings.NotificationSettingsScreen
import dk.zlatan.flotmand.Features.profile.switchlanguage.SwitchLanguageScreen
import dk.zlatan.flotmand.Features.profile.theme.ThemeSettingsScreen
import dk.zlatan.flotmand.design_system.componenets.PredictiveBackScaleContainer
import dk.zlatan.flotmand.util.ExternalLinks

@Suppress("CyclomaticComplexMethod")
@Composable
fun ProfileNavigation(viewModel: ProfileNavigationViewModel = hiltViewModel()) {
    val navigationStack: List<ProfileDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigation overlay for sub-screens
    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key ->
            when (key) {
                ProfileDestination.ProfileScreen -> {
                    NavEntry(key) {
                        ProfileScreenRoute(
                            navigateToAccountInformation = {
                                viewModel.navigate(ProfileDestination.AccountInformation)
                            },
                            onLanguageClick = {
                                viewModel.navigate(ProfileDestination.SwitchLanguage)
                            },
                            onNotificationSettingsClick = {
                                viewModel.navigate(ProfileDestination.NotificationSettings)
                            },
                            onThemeSettingsClick = {
                                viewModel.navigate(ProfileDestination.ThemeSettings)
                            },
                            onPrivacyPolicyClick = {
                                CustomTabsIntent.Builder().build()
                                    .launchUrl(context, ExternalLinks.PRIVACY_POLICY_URL.toUri())
                            },
                        )
                    }
                }

                ProfileDestination.AccountInformation -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            AccountInformationScreenRoute(
                                onDismiss = { viewModel.pop() },
                                onUserDeleted = { viewModel.resetToRoot() },
                                onOpenLicenses = { viewModel.navigate(ProfileDestination.Licenses) },
                            )
                        }
                    }
                }

                ProfileDestination.Licenses -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            LicensesScreen(onDismiss = { viewModel.pop() })
                        }
                    }
                }

                ProfileDestination.SwitchLanguage -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            SwitchLanguageScreen(
                                onDismiss = { viewModel.pop() },
                            )
                        }
                    }
                }

                ProfileDestination.NotificationSettings -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            NotificationSettingsScreen(
                                onDismiss = { viewModel.pop() },
                            )
                        }
                    }
                }

                ProfileDestination.ThemeSettings -> {
                    NavEntry(key) {
                        PredictiveBackScaleContainer {
                            ThemeSettingsScreen(
                                onDismiss = { viewModel.pop() },
                            )
                        }
                    }
                }

                else -> {
                    NavEntry(key) {
                        ProfileScreenRoute(
                            navigateToAccountInformation = {
                                viewModel.navigate(ProfileDestination.AccountInformation)
                            },
                            onLanguageClick = {
                                viewModel.navigate(ProfileDestination.SwitchLanguage)
                            },
                            onNotificationSettingsClick = {
                                viewModel.navigate(ProfileDestination.NotificationSettings)
                            },
                            onThemeSettingsClick = {
                                viewModel.navigate(ProfileDestination.ThemeSettings)
                            },
                            onPrivacyPolicyClick = {
                                CustomTabsIntent.Builder().build()
                                    .launchUrl(context, ExternalLinks.PRIVACY_POLICY_URL.toUri())
                            },
                        )
                    }
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                ),
                ExitTransition.None,
            )
        },
        popTransitionSpec = {
            ContentTransform(EnterTransition.None, ExitTransition.None)
        },
        predictivePopTransitionSpec = { _: Int ->
            ContentTransform(EnterTransition.None, ExitTransition.None)
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}
