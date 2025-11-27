package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer

@Composable
fun SectionItem(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector? = null,
    title: String,
    onClick: () -> Unit = { },
    onLongClick: () -> Unit = { }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    // Example: Open calendar app (placeholder, implement actual logic as needed)
                    onLongClick()
                }
            )
            .padding(vertical = 16.dp, horizontal = 20.dp)
        ,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null
        )
        HSpacer(15.dp)
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (trailingIcon != null){
            Icon(
                imageVector = trailingIcon,
                contentDescription = null
            )
        }
    }
}