package dk.zlatan.flotmand.Features.frontpage.datevotingDetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmConfirmDialog

@Composable
internal fun DeleteDialog(
 modifier: Modifier = Modifier,
 onDismiss: () -> Unit = {},
    onConfirmDelete: () -> Unit = {}
) {
    FmConfirmDialog(
        modifier = modifier,
        onDismiss = onDismiss,
        title = stringResource(R.string.delete_date_title),
        message = stringResource(R.string.delete_date_message),
        confirmText = stringResource(R.string.delete),
        dismissText = stringResource(R.string.cancel),
        onConfirmClick = onConfirmDelete,
    )
}

@Preview
@Composable
private fun DeleteDialogPreview() {
    DeleteDialog(modifier = Modifier)
}