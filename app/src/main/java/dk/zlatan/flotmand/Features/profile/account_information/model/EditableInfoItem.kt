package dk.zlatan.flotmand.Features.profile.account_information.model

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

internal data class EditableInfoItem(
    val label: String,
    val value: String,
    val isEditing: Boolean = false,
    val editedValue: TextFieldValue = TextFieldValue(),
    val onEditClick: () -> Unit = {},
    val onValueChange: (TextFieldValue) -> Unit = {},
    val onSave: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val canEdit: Boolean = true,
    val onDeleteClick: (() -> Unit)? = null,
    val placeholder: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
    val note: String? = null,
    val onNoteClick: (() -> Unit)? = null,
)

internal fun textFieldValueOf(text: String) =
    TextFieldValue(text = text, selection = TextRange(text.length))
