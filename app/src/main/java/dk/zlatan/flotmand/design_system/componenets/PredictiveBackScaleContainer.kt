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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation3.ui.LocalNavAnimatedContentScope

private const val MIN_SCALE = 0.88f
private const val MAX_CORNER_DP = 28f

// progress 0 → HOLD_END : gesture drag — scale + corners (unchanged feel)
// progress HOLD_END → 1 : spring after release — slide + fade, no further shrink
private const val HOLD_END = 0.8f
private const val SLIDE_DP = 72f

@Composable
fun PredictiveBackScaleContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animScope = LocalNavAnimatedContentScope.current
    val slideDistancePx = with(LocalDensity.current) { SLIDE_DP.dp.toPx() }

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

    // Phase 1: gesture drag — gentle scale and rounded corners
    val holdProgress = (progress / HOLD_END).coerceIn(0f, 1f)
    val scale = lerp(1f, MIN_SCALE, holdProgress)
    val cornerRadius = lerp(0f, MAX_CORNER_DP, holdProgress)

    // Phase 2: spring fires after release — small downward slide + fade, scale stays at MIN_SCALE
    val releaseProgress = ((progress - HOLD_END) / (1f - HOLD_END)).coerceIn(0f, 1f)
    val translationY = lerp(0f, slideDistancePx, releaseProgress)
    val alpha = lerp(1f, 0f, releaseProgress)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.translationY = translationY
                    this.alpha = alpha
                    clip = true
                    shape = RoundedCornerShape(cornerRadius.dp)
                },
    ) {
        content()
    }
}
