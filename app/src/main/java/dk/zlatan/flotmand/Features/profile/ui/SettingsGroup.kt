package dk.zlatan.flotmand.Features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

data class SettingsRowItem(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit,
    val trailingIcon: ImageVector = FmIcons.chevronRight,
    // Destructive rows (e.g. logout) render in error colors.
    val isDestructive: Boolean = false,
)

/** One card per settings section, rows separated by inset dividers. */
@Composable
internal fun SettingsGroup(
    items: List<SettingsRowItem>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                SettingsRow(item = item)
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsRowItem) {
    val accentColor =
        if (item.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .padding(9.dp)
                    .size(20.dp),
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (item.isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
            )
        }
        Icon(
            imageVector = item.trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsGroupPreview() {
    FlotMandTheme {
        SettingsGroup(
            items =
                listOf(
                    SettingsRowItem(icon = FmIcons.globe, title = "Sprog", onClick = {}),
                    SettingsRowItem(icon = FmIcons.darkMode, title = "Tema", onClick = {}),
                    SettingsRowItem(
                        icon = FmIcons.logout,
                        title = "Log ud",
                        onClick = {},
                        isDestructive = true,
                    ),
                ),
        )
    }
}