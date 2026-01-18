package dk.zlatan.flotmand.Features.profile.account_information.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.User
import kotlinx.coroutines.launch

/**
 * A card component that displays account details including user ID and authentication provider.
 * User ID can be toggled between visible and hidden states, and can be copied to clipboard
 * via long press.
 *
 * @param modifier Modifier for the card
 * @param user The user whose account details to display
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AccountDetailsCard(
    modifier: Modifier = Modifier,
    user: User,
) {
    val clipboardManager = LocalClipboardManager.current
    var isUserIdVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Konto Detaljer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            VSpacer(12.dp)

            // User ID with visibility toggle
            if (user.id.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bruger ID",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    VSpacer(4.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (isUserIdVisible) user.id else "••••••••",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = { },
                                        onLongClick = {
                                            clipboardManager.setText(AnnotatedString(user.id))
                                        },
                                    ),
                        )
                        IconButton(
                            onClick = { isUserIdVisible = !isUserIdVisible },
                            modifier = Modifier.padding(0.dp),
                        ) {
                            Icon(
                                imageVector = if (isUserIdVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isUserIdVisible) "Skjul ID" else "Vis ID",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            // Provider info
            if (user.provider.isNotEmpty()) {
                if (user.id.isNotEmpty()) {
                    VSpacer(12.dp)
                }
                InfoRow(
                    label = "Log ind med",
                    value = user.provider,
                )
            }
        }
    }
}
