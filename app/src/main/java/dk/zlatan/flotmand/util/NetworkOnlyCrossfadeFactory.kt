package dk.zlatan.flotmand.util

import coil.decode.DataSource
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.transition.CrossfadeTransition
import coil.transition.Transition
import coil.transition.TransitionTarget

// Only animate images that arrive over the network — memory and disk cache hits
// appear instantly so they don't look like a fresh load after every app restart.
object NetworkOnlyCrossfadeFactory : Transition.Factory {
    private val crossfade = CrossfadeTransition.Factory(durationMillis = 100)
    private val noOp =
        Transition.Factory { _, _ ->
            Transition { }
        }

    override fun create(
        target: TransitionTarget,
        result: ImageResult,
    ): Transition {
        val isNetworkLoad = result is SuccessResult && result.dataSource == DataSource.NETWORK
        return if (isNetworkLoad) crossfade.create(target, result) else noOp.create(target, result)
    }
}
