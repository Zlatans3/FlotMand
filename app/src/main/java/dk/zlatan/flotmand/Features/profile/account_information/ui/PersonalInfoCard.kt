package dk.zlatan.flotmand.Features.profile.account_information.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dk.zlatan.flotmand.Features.profile.account_information.model.EditableInfoItem
import dk.zlatan.flotmand.model.User

@Composable
internal fun PersonalInfoCard(
    modifier: Modifier = Modifier,
    user: User,
    isEditingDisplayName: Boolean,
    editedDisplayName: String,
    isEditingPhoneNumber: Boolean,
    editedPhoneNumber: String,
    isLoading: Boolean,
    onEditDisplayName: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSaveDisplayName: () -> Unit,
    onCancelDisplayName: () -> Unit,
    onEditPhoneNumber: () -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSavePhoneNumber: () -> Unit,
    onCancelPhoneNumber: () -> Unit
) {
    EditableInfoCard(
        modifier = modifier,
        title = "Personlige Oplysninger",
        items = buildList {
            if (user.displayName.isNotEmpty() || isEditingDisplayName) {
                add(
                    EditableInfoItem(
                        label = "Navn",
                        value = user.displayName,
                        isEditing = isEditingDisplayName,
                        editedValue = editedDisplayName,
                        onEditClick = onEditDisplayName,
                        onValueChange = onDisplayNameChange,
                        onSave = onSaveDisplayName,
                        onCancel = onCancelDisplayName
                    )
                )
            }
            if (user.email.isNotEmpty()) {
                add(
                    EditableInfoItem(
                        label = "Email",
                        value = user.email,
                        isEditing = false,
                        canEdit = false
                    )
                )
            }
            add(
                EditableInfoItem(
                    label = "Telefon",
                    value = user.phoneNumber,
                    isEditing = isEditingPhoneNumber,
                    editedValue = editedPhoneNumber,
                    onEditClick = onEditPhoneNumber,
                    onValueChange = onPhoneNumberChange,
                    onSave = onSavePhoneNumber,
                    onCancel = onCancelPhoneNumber,
                    placeholder = "Tilføj telefonnummer"
                )
            )
        },
        isLoading = isLoading
    )
}