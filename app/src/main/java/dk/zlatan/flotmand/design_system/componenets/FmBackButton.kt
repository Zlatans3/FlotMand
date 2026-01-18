package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun FmBackButton(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onBackClick,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "Tilbage",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun FmBackButtonPreview() {
    FmBackButton(
        modifier = Modifier,
        onBackClick = {},
    )
}
