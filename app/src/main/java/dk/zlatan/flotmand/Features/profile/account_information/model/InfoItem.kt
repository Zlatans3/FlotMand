package dk.zlatan.flotmand.Features.profile.account_information.model

internal data class InfoItem(
    val label: String,
    val value: String
)

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