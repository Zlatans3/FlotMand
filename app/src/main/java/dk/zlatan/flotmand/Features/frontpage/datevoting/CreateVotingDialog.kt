package dk.zlatan.flotmand.Features.frontpage.datevoting

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.R

@Composable
internal fun CreateVotingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxChar = 100
    var votingName by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        content = {
            Surface(
                modifier =
                    modifier
                        .fillMaxWidth(0.9f)
                        .background(MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.create_voting_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = stringResource(R.string.create_voting_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = votingName,
                        onValueChange = { if (it.length <= maxChar) votingName = it },
                        label = { Text(stringResource(R.string.create_voting_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused },
                        placeholder = { Text(stringResource(R.string.create_voting_placeholder)) },
                        singleLine = true,
                        maxLines = 1,
                        supportingText = {
                            AnimatedContent(
                                targetState = isFocused,
                                transitionSpec = {
                                    if (targetState) {
                                        slideInVertically { it } togetherWith slideOutVertically { -it }
                                    } else {
                                        slideInVertically { -it } togetherWith slideOutVertically { it }
                                    }
                                },
                            ) { focused ->
                                if (focused) {
                                    Text(
                                        text = "${votingName.length} / $maxChar",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                    )
                                } else {
                                    Text("")
                                }
                            }
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        TextButton(
                            onClick = onDismiss,
                        ) {
                            Text(stringResource(R.string.cancel))
                        }

                        Button(
                            onClick = {
                                onConfirm(votingName)
                                onDismiss()
                            },
                            enabled = votingName.isNotBlank(),
                        ) {
                            Text(stringResource(R.string.create))
                        }
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun CreateVotingDialogPreview() {
    FlotMandTheme {
        CreateVotingDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
