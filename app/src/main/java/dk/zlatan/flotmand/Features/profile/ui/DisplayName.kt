package dk.zlatan.flotmand.Features.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import kotlinx.coroutines.launch

@Composable
fun DisplayName(
    modifier: Modifier = Modifier,
    displayName: String,
    isLoading: Boolean,
    onUpdateDisplayNameClick: (String) -> Unit,
) {
    var showDisplayNameDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(displayName) }

    val scope = rememberCoroutineScope()

    Text(
        text = displayName,
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        showDisplayNameDialog = true
                    },
                ).padding(bottom = 20.dp),
    )

    if (showDisplayNameDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.profile_name)) },
            text = {
                Column {
                    TextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it },
                        enabled = !isLoading,
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDisplayNameDialog = false }, enabled = !isLoading) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        onUpdateDisplayNameClick(newDisplayName)
                        showDisplayNameDialog = false
                    }
                }, enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    } else {
                        Text(text = stringResource(R.string.update))
                    }
                }
            },
            onDismissRequest = { if (!isLoading) showDisplayNameDialog = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DisplayNamePreview() {
    DisplayName(
        displayName = "Zlatan Ibrahimovic",
        isLoading = false,
        onUpdateDisplayNameClick = {},
    )
}
