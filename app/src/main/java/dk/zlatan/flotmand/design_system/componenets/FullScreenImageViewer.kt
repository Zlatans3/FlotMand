package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.memory.MemoryCache
import coil.request.ImageRequest
import dk.zlatan.flotmand.util.FirebaseStorageInterceptor
import kotlin.math.abs
import kotlin.math.sign

// Material's SwipeToDismissBox velocity threshold.
private val FlingDismissVelocity = 125.dp
private val DragDismissDistance = 120.dp

/**
 * Displays an image edge-to-edge on a dark scrim. Dismiss by flinging or
 * dragging vertically (in either direction), tapping back, or tapping the scrim.
 */
@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    contentDescription: String? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        var offsetY by remember { mutableFloatStateOf(0f) }
        val density = LocalDensity.current
        val flingVelocityPx = with(density) { FlingDismissVelocity.toPx() }
        val dismissDistancePx = with(density) { DragDismissDistance.toPx() }
        var containerHeightPx by remember { mutableIntStateOf(0) }

        // Fade the scrim as the image is dragged towards the dismiss threshold.
        val scrimAlpha = (1f - abs(offsetY) / (dismissDistancePx * 4f)).coerceIn(0.3f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerHeightPx = it.height }
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta -> offsetY += delta },
                    onDragStopped = { velocity ->
                        val isFling = abs(velocity) > flingVelocityPx
                        val isPastThreshold = abs(offsetY) > dismissDistancePx
                        if (isFling || isPastThreshold) {
                            val direction = if (isFling) sign(velocity) else sign(offsetY)
                            animate(
                                initialValue = offsetY,
                                targetValue = direction * containerHeightPx,
                                initialVelocity = velocity,
                            ) { value, _ -> offsetY = value }
                            onDismiss()
                        } else {
                            animate(
                                initialValue = offsetY,
                                targetValue = 0f,
                                initialVelocity = velocity,
                            ) { value, _ -> offsetY = value }
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val context = LocalContext.current
            val stableKey = remember(imageUrl) { FirebaseStorageInterceptor.stableKey(imageUrl) }
            val model = remember(imageUrl) {
                ImageRequest.Builder(context)
                    .data(imageUrl)
                    .apply {
                        if (stableKey != null) {
                            memoryCacheKey(MemoryCache.Key(stableKey))
                            diskCacheKey(stableKey)
                        }
                    }
                    .build()
            }
            SubcomposeAsyncImage(
                model = model,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = offsetY },
                loading = {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                },
            )
        }
    }
}
