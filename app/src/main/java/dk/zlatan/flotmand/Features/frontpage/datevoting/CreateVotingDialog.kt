package dk.zlatan.flotmand.Features.frontpage.datevoting

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

@Composable
internal fun CreateVotingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var votingName by remember { mutableStateOf("") }

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
                        text = "Opret ny afstemning",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "Giv afstemningen et navn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = votingName,
                        onValueChange = { votingName = it },
                        label = { Text("Navn på afstemning") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("F.eks. hos David") },
                        singleLine = true,
                        maxLines = 1,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        TextButton(
                            onClick = onDismiss,
                        ) {
                            Text("Annuller")
                        }

                        Button(
                            onClick = {
                                onConfirm(votingName)
                                onDismiss()
                            },
                            enabled = votingName.isNotBlank(),
                        ) {
                            Text("Opret")
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
