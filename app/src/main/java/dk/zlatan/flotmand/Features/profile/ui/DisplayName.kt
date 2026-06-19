package dk.zlatan.flotmand.Features.profile.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DisplayName(
    modifier: Modifier = Modifier,
    displayName: String,
) {
    Text(
        text = displayName,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun DisplayNamePreview() {
    DisplayName(displayName = "Zlatan Ibrahimovic")
}
