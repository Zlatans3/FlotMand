package dk.zlatan.flotmand.design_system.componenets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.R

@Composable
fun FmAlertDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    onActionClick: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        title = { Text(stringResource(R.string.logout_confirm_title)) },
        text = { Text(stringResource(R.string.logout_confirm_message)) },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isLoading) { Text(text = stringResource(R.string.cancel)) }
        },
        confirmButton = {
            Button(onClick = onActionClick, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier) else Text(text = stringResource(R.string.logout))
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
fun FmConfirmDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String = stringResource(R.string.cancel),
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        title = { Text(title) },
        text = { Text(message) },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isLoading) { Text(text = dismissText) }
        },
        confirmButton = {
            Button(onClick = onConfirmClick, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier) else Text(text = confirmText)
            }
        },
        onDismissRequest = onDismiss
    )
}

@Preview
@Composable
private fun AlertDialogPreview() {
    FmAlertDialog(
        onDismiss = {},
        onActionClick = {}
    )
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    FmConfirmDialog(
        title = stringResource(R.string.delete_event_title),
        message = stringResource(R.string.delete_event_message),
        confirmText = stringResource(R.string.delete),
        onDismiss = {},
        onConfirmClick = {}
    )
}
