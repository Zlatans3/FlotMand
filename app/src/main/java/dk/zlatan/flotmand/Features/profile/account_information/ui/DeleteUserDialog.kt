package dk.zlatan.flotmand.Features.profile.account_information.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.R

@Composable
fun DeleteUserDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_user_title)) },
        text = {
            Text(text = stringResource(R.string.delete_user_message))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview
@Composable
private fun DeleteUserDialogPreview() {
    DeleteUserDialog(
        isLoading = false,
        onConfirm = {},
        onDismiss = {},
    )
}
