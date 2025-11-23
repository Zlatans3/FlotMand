package dk.zlatan.flotmand.Features.frontpage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dk.zlatan.flotmand.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer

@Composable
internal fun FrontPageHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 24.dp, // adjust for desired roundness
                    bottomEnd = 24.dp
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        VSpacer(90.dp)
        Image(
            painter = painterResource(id = R.drawable.flotmandapp),
            contentDescription = "Flotmand Logo",
            modifier = Modifier
                .size(120.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Text(
            text = "Forside",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        VSpacer(12.dp)

    }
}

@Preview
@Composable
private fun FrontPageHeaderPreview() {
    FrontPageHeader(modifier = Modifier)
}
