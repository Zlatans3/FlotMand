package dk.zlatan.flotmand.design_system.componenets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog

@Composable
fun FmAlertDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSignOutClick: () -> Unit,

    ) {
    AlertDialog(
        title = { Text("Er du sikker på du vil logge ud?") },
        text = { Text("Hvis du logger ud, kan du ikke se de flotte mænds beskeder") },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Annuller")
            }
        },
        confirmButton = {
            Button(onClick = {
                onSignOutClick()
            }) {
                Text(text = "Log ud")
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