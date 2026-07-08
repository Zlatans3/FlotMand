package dk.zlatan.flotmand.Features.authentication.login

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import dk.zlatan.flotmand.BuildConfig
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import kotlinx.coroutines.launch

@Composable
internal fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState(initial = LoginUiState())

    // Trigger navigation when login is successful
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LoginContent(
        modifier = modifier.fillMaxSize(),
        onGoogleLoginClick = { credential ->
            viewModel.onSignInWithGoogle(credential)
        },
        onError = { message ->
            viewModel.setError(message)
        },
        uiState = uiState,
        onRetry = { viewModel.clearError() },
    )
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    onGoogleLoginClick: (Credential) -> Unit = {},
    onError: (String) -> Unit = {},
    uiState: LoginUiState = LoginUiState(),
    onRetry: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPrivacySheet by rememberSaveable { mutableStateOf(false) }

    if (showPrivacySheet) {
        PrivacyConsentBottomSheet(
            onAccept = {
                showPrivacySheet = false
                scope.launch {
                    launchCredManButtonUI(
                        context = context,
                        onRequestResult = onGoogleLoginClick,
                        onError = onError,
                    )
                }
            },
            onDismiss = { showPrivacySheet = false },
        )
    }

    // Runs the entrance animations once, when the screen first appears.
    val entrance = remember { MutableTransitionState(false).apply { targetState = true } }

    val backgroundBrush = Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.primaryContainer,
        0.65f to MaterialTheme.colorScheme.background,
    )

    Box(
        modifier = modifier
            .background(backgroundBrush)
            .systemBarsPadding(),
    ) {
        AnimatedVisibility(
            visibleState = entrance,
            enter = fadeIn(tween(durationMillis = 700)) +
                scaleIn(initialScale = 0.85f, animationSpec = tween(durationMillis = 700)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            BrandBlock(modifier = Modifier.padding(horizontal = 32.dp))
        }

        AnimatedVisibility(
            visibleState = entrance,
            enter = fadeIn(tween(durationMillis = 500, delayMillis = 250)) +
                slideInVertically(tween(durationMillis = 500, delayMillis = 250)) { it / 3 },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                uiState.errorMessage?.let { message ->
                    ErrorCard(message = message, onRetry = onRetry)
                    VSpacer(16.dp)
                }

                GoogleLoginButton(
                    isLoading = uiState.isLoading,
                    onClick = { showPrivacySheet = true },
                )

                VSpacer(12.dp)

                Text(
                    text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Show loading indicator overlay if loading
        if (uiState.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000)),
                // semi-transparent overlay
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun BrandBlock(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        ) {
            Image(
                painter = painterResource(id = R.drawable.flotmandapp),
                contentDescription = stringResource(R.string.flotmand_logo_content_description),
                modifier = Modifier
                    .padding(24.dp)
                    .size(96.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
        }

        VSpacer(24.dp)

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        VSpacer(8.dp)

        Text(
            text = stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GoogleLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(6.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_g_logo),
                    contentDescription = stringResource(R.string.google_logo_content_description),
                    modifier = Modifier.size(22.dp),
                )
            }

            HSpacer(16.dp)

            Text(
                text = stringResource(R.string.login_with_google),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: (() -> Unit)?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(
                        text = stringResource(R.string.retry),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

// Launches the Credential Manager UI for Google Sign-In and returns the result via callbacks
private suspend fun launchCredManButtonUI(
    context: Context,
    onRequestResult: (Credential) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val serverClientId = context.getString(R.string.default_web_client_id)
        if (serverClientId.isBlank()) {
            onError(context.getString(R.string.error_sign_in_generic))
            return
        }

        val signInWithGoogleOption =
            GetSignInWithGoogleOption
                .Builder(serverClientId = serverClientId)
                .build()

        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

        val result =
            CredentialManager.create(context).getCredential(
                request = request,
                context = context,
            )

        onRequestResult(result.credential)
    } catch (e: GetCredentialCancellationException) {
        Log.w("LOGIN", "Credential flow cancelled or failed silently: ${e.message}", e)
    } catch (e: NoCredentialException) {
        Log.e("LOGIN", e.message.orEmpty())
        onError(context.getString(R.string.error_no_google_account))
    } catch (e: GetCredentialException) {
        Log.e("LOGIN", "GetCredentialException type=${e::class.java.simpleName} msg=${e.message}", e)
        onError(context.getString(R.string.error_sign_in_generic))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    FlotMandTheme {
        LoginContent(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenErrorPreview() {
    FlotMandTheme {
        LoginContent(
            modifier = Modifier.fillMaxSize(),
            uiState = LoginUiState(errorMessage = "Der opstod en fejl under login"),
            onRetry = {},
        )
    }
}