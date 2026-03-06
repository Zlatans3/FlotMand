package dk.zlatan.flotmand.Features.authentication.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.FlotHeader
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import kotlinx.coroutines.launch

@Composable
internal fun LoginRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
) {
    LoginScreen(
        modifier = modifier,
        viewModel = viewModel,
        onLoginSuccess = onLoginSuccess
    )
}

@Composable
private fun LoginScreen(
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
        uiState = uiState,
        onRetry = {
            // Retry logic: clear error and show login again
            viewModel.clearError() // You need to implement this in your ViewModel
        }
    )
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    onGoogleLoginClick: (Credential) -> Unit = {},
    uiState: LoginUiState = LoginUiState(),
    onRetry: (() -> Unit)? = null // Add retry callback
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {

            FlotHeader(
                modifier = Modifier.fillMaxWidth(),
                headerTitle = stringResource(R.string.login_title),
                headerTopPadding = 200.dp
            )

            VSpacer(20.dp)

            LoginCard(
                isLoading = uiState.isLoading,
                onGoogleLoginClick = onGoogleLoginClick
            )

            VSpacer(20.dp)

            // Optionally show error
            uiState.errorMessage?.let {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    // Show retry button if callback provided
                    if (onRetry != null) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = onRetry) {
                            Text(text = stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
        // Show loading indicator overlay if loading
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)), // semi-transparent overlay
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun LoginCard(
    isLoading: Boolean,
    onGoogleLoginClick: (Credential) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clickable(enabled = !isLoading, onClick = {  scope.launch {
                launchCredManButtonUI(
                    context,
                    onGoogleLoginClick
                )
            }}),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                    .padding(6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_g_logo),
                    contentDescription = stringResource(R.string.google_logo_content_description),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.login_with_google),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// Launches the Credential Manager UI for Google Sign-In
private suspend fun launchCredManButtonUI(
    context: Context,
    onRequestResult: (Credential) -> Unit
) {
    try {
        val signInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val result = CredentialManager.create(context).getCredential(
            request = request,
            context = context
        )

        onRequestResult(result.credential)
    } catch (e: NoCredentialException) {
        Log.e("ERROR_TAG", e.message.orEmpty())
//        SnackbarManager.showMessage(context.getString(R.string.no_accounts_error))
    } catch (e: GetCredentialException) {
        Log.d("ERROR_TAG", e.message.orEmpty())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    FlotMandTheme() {
        LoginContent(modifier = Modifier)
    }
}