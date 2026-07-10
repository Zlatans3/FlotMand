package dk.zlatan.flotmand.Features.profile.account_information.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.Features.profile.account_information.model.EditableInfoItem
import dk.zlatan.flotmand.Features.profile.account_information.model.textFieldValueOf
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.User

@Composable
internal fun PersonalInfoCard(
    modifier: Modifier = Modifier,
    user: User,
    isLoading: Boolean = false,
    onUpdateDisplayName: (String) -> Unit = {},
    onUpdatePhoneNumber: (String) -> Unit = {},
    onDeletePhoneNumber: () -> Unit = {},
) {
    var isEditingDisplayName by remember { mutableStateOf(false) }
    var editedDisplayName by remember(user.displayName) {
        mutableStateOf(textFieldValueOf(user.displayName))
    }
    var isEditingPhoneNumber by remember { mutableStateOf(false) }
    var editedPhoneNumber by remember(user.phoneNumber) {
        mutableStateOf(textFieldValueOf(user.phoneNumber.formatDanishPhone()))
    }
    var showPhoneConsentDialog by remember { mutableStateOf(false) }
    var openEditorOnConsent by remember { mutableStateOf(false) }

    EditableInfoCard(
        modifier = modifier,
        items =
            buildList {
                if (user.displayName.isNotEmpty() || isEditingDisplayName) {
                    add(
                        EditableInfoItem(
                            label = stringResource(R.string.personal_info_name),
                            value = editedDisplayName.text,
                            isEditing = isEditingDisplayName,
                            editedValue = editedDisplayName,
                            onEditClick = { isEditingDisplayName = true },
                            onValueChange = { editedDisplayName = it },
                            onSave = {
                                onUpdateDisplayName(editedDisplayName.text)
                                isEditingDisplayName = false
                            },
                            onCancel = {
                                editedDisplayName = textFieldValueOf(user.displayName)
                                isEditingDisplayName = false
                            },
                        ),
                    )
                }
                if (user.email.isNotEmpty()) {
                    add(
                        EditableInfoItem(
                            label = stringResource(R.string.personal_info_email),
                            value = user.email,
                            canEdit = false,
                        ),
                    )
                }
                add(
                    EditableInfoItem(
                        label = stringResource(R.string.personal_info_phone),
                        value = editedPhoneNumber.text,
                        isEditing = isEditingPhoneNumber,
                        editedValue = editedPhoneNumber,
                        onEditClick = {
                            if (user.phoneNumber.isBlank()) {
                                openEditorOnConsent = true
                                showPhoneConsentDialog = true
                            } else {
                                isEditingPhoneNumber = true
                            }
                        },
                        onDeleteClick = if (user.phoneNumber.isNotBlank()) onDeletePhoneNumber else null,
                        onValueChange = { input ->
                            editedPhoneNumber = formatPhoneInput(input, editedPhoneNumber)
                        },
                        onSave = {
                            onUpdatePhoneNumber(editedPhoneNumber.text.filter { it.isDigit() })
                            isEditingPhoneNumber = false
                        },
                        onCancel = {
                            editedPhoneNumber = textFieldValueOf(user.phoneNumber.formatDanishPhone())
                            isEditingPhoneNumber = false
                        },
                        placeholder = stringResource(R.string.personal_info_add_phone),
                        keyboardType = KeyboardType.Phone,
                        note = stringResource(R.string.personal_info_phone_note),
                        onNoteClick = {
                            openEditorOnConsent = false
                            showPhoneConsentDialog = true
                        },
                    ),
                )
            },
        isLoading = isLoading,
    )

    if (showPhoneConsentDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneConsentDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.phone_consent_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.phone_consent_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPhoneConsentDialog = false
                        if (openEditorOnConsent) isEditingPhoneNumber = true
                    },
                ) {
                    Text(stringResource(R.string.phone_consent_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneConsentDialog = false }) {
                    Text(stringResource(R.string.phone_consent_dialog_no_thanks))
                }
            },
        )
    }
}

private fun formatPhoneInput(input: TextFieldValue, previous: TextFieldValue): TextFieldValue {
    val rawText = input.text
    val cursorPos = input.selection.end
    val previousDigitCount = previous.text.count { it.isDigit() }
    val rawDigitsBeforeCursor = rawText.take(cursorPos).count { it.isDigit() }

    var digits = rawText.filter { it.isDigit() }.take(8)

    // User deleted a formatting space: digit count is unchanged but text got shorter.
    // Remove the digit that preceded the space instead.
    if (rawText.length < previous.text.length && digits.length == previousDigitCount && rawDigitsBeforeCursor > 0) {
        digits = digits.removeRange(rawDigitsBeforeCursor - 1, rawDigitsBeforeCursor)
    }

    val formatted = digits.formatDanishPhone()

    // Map digit cursor position → formatted cursor position.
    // Spaces are inserted after every pair: "12 34 56 78"
    // formatted_pos(n digits before cursor) = n + (n - 1) / 2  (integer division)
    val n = rawDigitsBeforeCursor.coerceAtMost(digits.length)
    val newCursor = if (n == 0) 0 else n + (n - 1) / 2

    return TextFieldValue(
        text = formatted,
        selection = TextRange(newCursor.coerceAtMost(formatted.length)),
    )
}

private fun String.formatDanishPhone(): String {
    val digits = filter { it.isDigit() }.take(8)
    return buildString {
        digits.forEachIndexed { i, c ->
            if (i > 0 && i % 2 == 0) append(' ')
            append(c)
        }
    }
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
