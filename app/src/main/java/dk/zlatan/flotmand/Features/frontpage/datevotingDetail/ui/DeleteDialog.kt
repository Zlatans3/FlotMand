package dk.zlatan.flotmand.Features.frontpage.datevotingDetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmAlertDialog
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
        title = "Slet dato",
        message = "Er du sikker på du vil slette denne dato? Denne handling kan ikke fortrydes.",
        confirmText = "Slet",
        dismissText = "Annuller",
        onConfirmClick = onConfirmDelete,
    )
}

@Preview
@Composable
private fun DeleteDialogPreview() {
    DeleteDialog(modifier = Modifier)
}