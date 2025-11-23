package dk.zlatan.flotmand.design_system.componenets.spacers

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VSpacer(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = modifier.height(height))
}

@Composable
fun HSpacer(
    width: Dp,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = modifier.width(width))
}