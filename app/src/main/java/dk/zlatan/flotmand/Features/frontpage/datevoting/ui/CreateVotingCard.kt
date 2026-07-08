package dk.zlatan.flotmand.Features.frontpage.datevoting.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

private const val VOTING_NAME_MAX_LENGTH = 100

/**
 * Inline create-voting form shown in place of the create CTA — same expand-in-place
 * pattern as the add-price card on the event detail screen.
 */
@Composable
internal fun CreateVotingCard(
    onCancel: () -> Unit,
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var votingName by rememberSaveable { mutableStateOf("") }
    var isFocused by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.create_voting_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        VSpacer(4.dp)

        Text(
            text = stringResource(R.string.create_voting_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        VSpacer(12.dp)

        OutlinedTextField(
            value = votingName,
            onValueChange = { if (it.length <= VOTING_NAME_MAX_LENGTH) votingName = it },
            label = { Text(stringResource(R.string.create_voting_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            placeholder = { Text(stringResource(R.string.create_voting_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
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
                            text = "${votingName.length} / $VOTING_NAME_MAX_LENGTH",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    } else {
                        Text("")
                    }
                }
            },
        )

        VSpacer(8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
            HSpacer(8.dp)
            Button(
                onClick = { onCreate(votingName) },
                enabled = votingName.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.create))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateVotingCardPreview() {
    FlotMandTheme {
        CreateVotingCard(
            onCancel = {},
            onCreate = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
