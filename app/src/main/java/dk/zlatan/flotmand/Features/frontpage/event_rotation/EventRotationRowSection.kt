package dk.zlatan.flotmand.Features.frontpage.event_rotation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer

// Data class for rotation item
data class RotationItem(
    val imageRes: Int,
    val name: String,
)

@Composable
fun RotationImagesAndNames(
    modifier: Modifier = Modifier,
    items: List<RotationItem> = sampleRotationItems,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier =
            modifier
                .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // TODO: Zlatan 17/01/2026 Change
        VSpacer(1.dp)
        items.forEach { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    ),
                onClick = {
                    // TODO: Zlatan 17/01/2026 Some release
                },
                modifier =
                    Modifier
                        .padding(vertical = 8.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .widthIn(min = 72.dp, max = 96.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.name,
                            modifier =
                                Modifier
                                    .size(60.dp)
                                    .shadow(
                                        elevation = 2.dp,
                                        shape = CircleShape,
                                    ),
                        )
                    }
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(end = 8.dp))
    }
}

// Sample data (replace with real drawables in your project)
val sampleRotationItems =
    listOf(
        RotationItem(R.drawable.david_loading, "David"),
        RotationItem(R.drawable.oliver_loading, "Oliver"),
        RotationItem(R.drawable.lasse_loading, "Lasse"),
        RotationItem(R.drawable.gustav_loading, "Gustav"),
        RotationItem(R.drawable.mikkel_loading, "Mikkel"),
        RotationItem(R.drawable.zlatan_loading, "Zlatan"),
        RotationItem(R.drawable.elias_loading, "Elias"),
    )

@Preview(showBackground = true)
@Composable
private fun RotationImagesAndNamesPreview() {
    RotationImagesAndNames()
}
