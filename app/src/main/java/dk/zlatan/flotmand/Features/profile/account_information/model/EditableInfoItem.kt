package dk.zlatan.flotmand.Features.profile.account_information.model

/**
 * Data model for an editable information field in the account information screen.
 *
 * @param label The display label for this field
 * @param value The current value of the field
 * @param isEditing Whether this field is currently being edited
 * @param editedValue The edited value while in edit mode
 * @param onEditClick Callback when edit button is clicked
 * @param onValueChange Callback when the value is changed during editing
 * @param onSave Callback when save button is clicked
 * @param onCancel Callback when cancel button is clicked
 * @param canEdit Whether this field can be edited
 * @param placeholder Placeholder text to show when the field is empty
 */
internal data class EditableInfoItem(
    val label: String,
    val value: String,
    val isEditing: Boolean = false,
    val editedValue: String = "",
    val onEditClick: () -> Unit = {},
    val onValueChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val canEdit: Boolean = true,
    val placeholder: String = ""
)
