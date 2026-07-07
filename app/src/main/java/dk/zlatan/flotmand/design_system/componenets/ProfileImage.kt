package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import dk.zlatan.flotmand.util.FirebaseStorageInterceptor

@Composable
fun ProfileImage(
    modifier: Modifier = Modifier,
    profilePic: String? = null,
    profileSize: Dp = 40.dp,
    userName: String,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val optionalClickableModifier =
        if (onClick != null || onLongClick != null) {
            Modifier.combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick,
            )
        } else {
            Modifier
        }
    val baseModifier = modifier
        .clip(CircleShape)
        .then(optionalClickableModifier)
        .size(profileSize)

    if (!profilePic.isNullOrBlank()) {
        val context = LocalContext.current
        val stableKey = remember(profilePic) { FirebaseStorageInterceptor.stableKey(profilePic) }

        // Check Coil's memory cache synchronously at composition time.
        // SubcomposeAsyncImage always starts in Loading state and resolves in a coroutine —
        // even for memory cache hits — causing at least one frame of the loading state.
        // By checking here we can skip the async path entirely for already-loaded images.
        // We search by key string so extras (e.g. coil#is_sampled) don't prevent a match.
        val cachedBitmap = remember(stableKey) {
            stableKey?.let { key ->
                val memCache = context.imageLoader.memoryCache
                memCache?.keys?.firstOrNull { it.key == key }?.let { memCache[it] }?.bitmap
            }
        }

        if (cachedBitmap != null) {
            Image(
                painter = remember(cachedBitmap) { BitmapPainter(cachedBitmap.asImageBitmap()) },
                contentDescription = "Profile Image",
                modifier = baseModifier,
            )
        } else {
            val model = remember(profilePic) {
                ImageRequest.Builder(context)
                    .data(profilePic)
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
                contentDescription = "Profile Image",
                modifier = baseModifier,
                loading = { InitialsBadge(userName, profileSize) },
                error = { InitialsBadge(userName, profileSize) },
            )
        }
    } else {
        InitialsBadge(userName, profileSize, baseModifier)
    }
}

@Composable
private fun InitialsBadge(
    userName: String,
    profileSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.inversePrimary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = getInitials(userName),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = (profileSize.value / 2).sp,
        )
    }
}

@Preview
@Composable
private fun ProfileImagePreview() {
    ProfileImage(
        modifier = Modifier,
        profilePic = null,
        userName = "Oliver Pain",
    )
}

private fun getInitials(userName: String): String {
    val parts = userName.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 ->
            (parts[0].firstOrNull()?.toString().orEmpty()) +
                (parts[1].firstOrNull()?.toString().orEmpty())
        parts.size == 1 && parts[0].length >= 2 -> parts[0].substring(0, 2)
        parts.size == 1 && parts[0].isNotEmpty() -> parts[0].substring(0, 1)
        else -> "--"
    }.uppercase()
}
