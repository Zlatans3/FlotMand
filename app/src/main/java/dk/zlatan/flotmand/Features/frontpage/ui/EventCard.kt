package dk.zlatan.flotmand.Features.frontpage.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import java.sql.Time

@Composable
internal fun EventCard(
    modifier: Modifier = Modifier,
    @DrawableRes userProfilePic: Int,
    userName: String,
    eventDate: String,
    eventTime: String,

    ) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(), // find nogle farver der passer bedre
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        VSpacer(height = 20.dp)

        Row() {
            Image(
                painter = painterResource(userProfilePic),
                contentDescription = "planer profile picture",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview() {
    EventCard(
        modifier = Modifier,

    )
}