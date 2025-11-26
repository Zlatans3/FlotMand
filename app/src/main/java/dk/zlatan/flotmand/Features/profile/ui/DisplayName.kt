package dk.zlatan.flotmand.Features.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import kotlinx.coroutines.launch

@Composable
fun DisplayName(
    modifier: Modifier = Modifier,
    displayName: String,
    onUpdateDisplayNameClick: (String) -> Unit
    ) {
    var showDisplayNameDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(displayName) }

    val scope = rememberCoroutineScope()

    val cardTitle = displayName.ifBlank { stringResource(R.string.profile_name) }

    Text(
        text = "Hej!\n$displayName",
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
            onClick = {
                showDisplayNameDialog = true
            }
        )
            .padding(bottom = 20.dp)
    )

    if (showDisplayNameDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.profile_name)) },
            text = {
                Column {
                    TextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it }
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDisplayNameDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        onUpdateDisplayNameClick(newDisplayName)
                        showDisplayNameDialog = false
                    }
                }) {
                    Text(text = stringResource(R.string.update))
                }
            },
            onDismissRequest = { showDisplayNameDialog = false }
        )
    }
}