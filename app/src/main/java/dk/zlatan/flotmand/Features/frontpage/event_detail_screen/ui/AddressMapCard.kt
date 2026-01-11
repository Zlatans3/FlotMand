package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.AddressMapCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.GeoLocation

@Composable
fun AddressMapCard(
    addressName: String,
    cityName: String,
    onCardClick: () -> Unit,
    geoLocation: GeoLocation,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(6),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Map with badges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .height(200.dp)
        ) {
            AddressMapCard(
                modifier = Modifier.fillMaxSize(),
                geoLocation = geoLocation,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = onCardClick
            )

        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clickable(onClick = {
                    val fullAddress = "$addressName, $cityName"
                    clipboardManager.setText(AnnotatedString(fullAddress))
                }
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = addressName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    VSpacer(6.dp)
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Kopier adresse",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddressMapCardPreview() {
    AddressMapCard(
        addressName = "Nyhavn 17",
        cityName = "1051 København K",
        onCardClick = {},
        geoLocation = GeoLocation(latitude = 55.6761, longitude = 12.5683), // Copenhagen
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(16.dp)
    )
}