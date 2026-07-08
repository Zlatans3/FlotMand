package dk.zlatan.flotmand.Features.authentication.login

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.util.ExternalLinks
import kotlinx.coroutines.launch

/**
 * Asks the user to accept the privacy policy before the sign-in flow starts.
 * Accept animates the sheet away before invoking [onAccept].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrivacyConsentBottomSheet(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.privacy_policy),
                style = MaterialTheme.typography.headlineSmall,
            )

            VSpacer(12.dp)

            Text(
                text = stringResource(R.string.login_privacy_sheet_text),
                style = MaterialTheme.typography.bodyMedium,
            )

            VSpacer(12.dp)

            Text(
                text = stringResource(R.string.login_privacy_read_policy),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    CustomTabsIntent.Builder().build()
                        .launchUrl(context, ExternalLinks.PRIVACY_POLICY_URL.toUri())
                },
            )

            VSpacer(24.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onAccept() }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.accept))
                }
            }

            VSpacer(16.dp)
        }
    }
}