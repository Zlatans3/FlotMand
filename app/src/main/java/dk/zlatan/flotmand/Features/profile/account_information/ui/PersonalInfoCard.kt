package dk.zlatan.flotmand.Features.profile.account_information.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.Features.profile.account_information.model.EditableInfoItem
import dk.zlatan.flotmand.model.User

/**
 * A card component that displays personal information (display name, email, phone number).
 * Provides inline editing capabilities for editable fields. Manages editing state internally.
 *
 * @param modifier Modifier for the card
 * @param user The user whose information to display
 * @param isLoading Whether the card is in a loading state
 * @param onUpdateDisplayName Callback to save updated display name
 * @param onUpdatePhoneNumber Callback to save updated phone number
 */
@Composable
internal fun PersonalInfoCard(
    modifier: Modifier = Modifier,
    user: User,
    isLoading: Boolean = false,
    onUpdateDisplayName: (String) -> Unit = {},
    onUpdatePhoneNumber: (String) -> Unit = {}
) {
    var isEditingDisplayName by remember { mutableStateOf(false) }
    var editedDisplayName by remember(user.displayName) { mutableStateOf(user.displayName) }
    var isEditingPhoneNumber by remember { mutableStateOf(false) }
    var editedPhoneNumber by remember(user.phoneNumber) { mutableStateOf(user.phoneNumber) }

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
                        onEditClick = { isEditingDisplayName = true },
                        onValueChange = { editedDisplayName = it },
                        onSave = {
                            onUpdateDisplayName(editedDisplayName)
                            isEditingDisplayName = false
                        },
                        onCancel = {
                            editedDisplayName = user.displayName
                            isEditingDisplayName = false
                        }
                    )
                )
            }
            if (user.email.isNotEmpty()) {
                add(
                    EditableInfoItem(
                        label = "Email",
                        value = user.email,
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
                    onEditClick = { isEditingPhoneNumber = true },
                    onValueChange = { editedPhoneNumber = it },
                    onSave = {
                        onUpdatePhoneNumber(editedPhoneNumber)
                        isEditingPhoneNumber = false
                    },
                    onCancel = {
                        editedPhoneNumber = user.phoneNumber
                        isEditingPhoneNumber = false
                    },
                    placeholder = "Tilføj telefonnummer",
                    keyboardType = KeyboardType.Phone
                )
            )
        },
        isLoading = isLoading
    )
}

@Preview
@Composable
private fun PersonalInfoCardPreview() {
    val user = User.mockUserWithCounter(1).first()
    PersonalInfoCard(
        user = user,
        isLoading = false,
    )
}
