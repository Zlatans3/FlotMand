package dk.zlatan.flotmand.Features.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

@Composable
internal fun SectionCard(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    iconRes: ImageVector = FmIcons.Person,
    trailingIcon: ImageVector = FmIcons.chevronRight,
    title: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .clickable(enabled = !isLoading, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = iconRes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionCardPreview() {
    FlotMandTheme {
        SectionCard(
            modifier = Modifier,
            isLoading = false,
            onClick = {},
            title = "Kontoinformation",
        )
    }
}
