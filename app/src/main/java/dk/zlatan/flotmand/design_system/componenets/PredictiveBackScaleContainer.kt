package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation3.ui.LocalNavAnimatedContentScope

private const val MIN_SCALE = 0.88f
private const val MAX_CORNER_DP = 28f

@Composable
fun PredictiveBackScaleContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animScope = LocalNavAnimatedContentScope.current
    val progress by animScope.transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            )
        },
        label = "PredictiveBackProgress",
    ) { state ->
        if (state == EnterExitState.PostExit) 1f else 0f
    }

    val scale = lerp(1f, MIN_SCALE, progress)
    val cornerRadius = lerp(0f, MAX_CORNER_DP, progress)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    clip = true
                    shape = RoundedCornerShape(cornerRadius.dp)
                },
    ) {
        content()
    }
}
