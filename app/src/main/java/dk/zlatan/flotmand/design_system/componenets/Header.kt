package dk.zlatan.flotmand.design_system.componenets

import android.icu.text.CaseMap
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

@Composable
internal fun Header(
    modifier: Modifier = Modifier,
    headerTitle: String,
    headerTopPadding: Dp = 90.dp
    ) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        VSpacer(headerTopPadding)
        Image(
            painter = painterResource(id = R.drawable.flotmandapp),
            contentDescription = "Flotmand Logo",
            modifier = Modifier
                .size(120.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Text(
            text = headerTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        VSpacer(12.dp)

    }
}

@Preview
@Composable
private fun FrontPageHeaderPreview() {
    FlotMandTheme() {
        Header(
            modifier = Modifier,
            headerTitle = "Forside"
        )
    }
}
