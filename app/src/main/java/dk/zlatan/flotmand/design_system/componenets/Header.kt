package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

@Composable
fun FlotHeader(
    modifier: Modifier = Modifier,
    headerTitle: String,
    headerTopPadding: Dp = 90.dp,
    ) {
    HeaderContainer(modifier = modifier) {
        VSpacer(headerTopPadding)
        Image(
            painter = painterResource(id = R.drawable.flotmandapp),
            contentDescription = stringResource(R.string.flotmand_logo_content_description),
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

@Composable
internal fun HeaderContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
    ) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                )
            )
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
       content()
    }
}

@Preview
@Composable
private fun FrontPageFlotHeaderContainerPreview() {
    FlotMandTheme() {
        FlotHeader(
            modifier = Modifier,
            headerTitle = stringResource(R.string.frontpage_title)
        )
    }
}
