package dk.zlatan.flotmand.design_system.componenets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FmAlertDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    onSignOutClick: () -> Unit,
) {
    AlertDialog(
        title = { Text("Er du sikker på du vil logge ud?") },
        text = { Text("Hvis du logger ud, kan du ikke se de flotte mænds beskeder") },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isLoading) { Text(text = "Annuller") }
        },
        confirmButton = {
            Button(onClick = onSignOutClick, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier) else Text(text = "Log ud")
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
    dismissText: String = "Annuller",
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
        onSignOutClick = {}
    )
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    FmConfirmDialog(
        title = "Slet event?",
        message = "Denne handling kan ikke fortrydes.",
        confirmText = "Slet",
        onDismiss = {},
        onConfirmClick = {}
    )
}
