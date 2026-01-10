package dk.zlatan.flotmand.Features.my_events.add_new_event.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

@Composable
fun EventTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        VSpacer(8.dp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    }
                ),
            placeholder = { Text(placeholder) },
            readOnly = onClick != null,
            enabled = onClick == null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines
        )
    }
}

/**
 * EventTextField overload that accepts TextFieldValue for cursor position control.
 */
@Composable
fun EventTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable (() -> Unit))? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        VSpacer(8.dp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventTextFieldPreview() {
    FlotMandTheme {
        EventTextField(
            label = "Event navn",
            value = "",
            maxLines = 1,
            onValueChange = { },
            placeholder = "Fx. Middag hos Gustav",
        )
    }
}