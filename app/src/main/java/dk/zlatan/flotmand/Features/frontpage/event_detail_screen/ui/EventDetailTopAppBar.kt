package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.design_system.componenets.FmBackButton
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDetailTopAppBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    isPublisher: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    FmTopAppBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        leadingIcon = {
            FmBackButton(
                onBackClick = onBackClick,
            )
        },
        trailingContent = {
            if (isPublisher) {
                Row(
                    modifier = Modifier,
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rediger event",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Slet event",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun EventDetailTopAppBarPreview() {
    EventDetailTopAppBar(
        modifier = Modifier,
        onBackClick = { },
        isPublisher = true,
        onDeleteClick = { },
        onEditClick = { },
    )
}
