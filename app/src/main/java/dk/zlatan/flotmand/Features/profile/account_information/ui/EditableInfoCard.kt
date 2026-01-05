package dk.zlatan.flotmand.Features.profile.account_information.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.Features.profile.account_information.model.EditableInfoItem
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer

/**
 * A card component that displays a list of editable information fields.
 * Supports inline editing with save/cancel actions and loading states.
 *
 * @param title The title displayed at the top of the card
 * @param items List of editable information items to display
 * @param isLoading Whether the card is in a loading state
 */
@Composable
internal fun EditableInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    items: List<EditableInfoItem>,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            VSpacer(12.dp)

            items.forEachIndexed { index, item ->
                EditableInfoRow(
                    item = item,
                    isLoading = isLoading
                )
                if (index < items.size - 1) {
                    VSpacer(12.dp)
                }
            }
        }
    }
}

@Composable
private fun EditableInfoRow(
    modifier: Modifier = Modifier,
    item: EditableInfoItem,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        VSpacer(4.dp)

        AnimatedVisibility(
            visible = item.isEditing,
            enter = fadeIn() + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = fadeOut() + shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Column {
                OutlinedTextField(
                    value = item.editedValue,
                    onValueChange = item.onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        if (item.placeholder.isNotEmpty()) {
                            Text(item.placeholder)
                        }
                    },
                    enabled = !isLoading,
                    singleLine = true
                )
                VSpacer(8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = item.onCancel,
                        enabled = !isLoading
                    ) {
                        Text("Annuller")
                    }
                    Button(
                        onClick = item.onSave,
                        enabled = !isLoading,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Gem")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !item.isEditing,
            enter = fadeIn() + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut() + shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.value.ifEmpty { "Ikke angivet" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.value.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )
                if (item.canEdit) {
                    IconButton(
                        onClick = item.onEditClick,
                        enabled = !isLoading,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rediger ${item.label}",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}