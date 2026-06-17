package dk.zlatan.flotmand.util

import android.net.Uri
import coil.intercept.Interceptor
import coil.memory.MemoryCache
import coil.request.ImageResult

// Firebase Storage download URLs contain a rotating ?token=... query param.
// Without this interceptor, Coil treats every new URL as a cache miss and re-downloads
// the same image. We normalise the cache keys to the stable base URL so the same file
// always hits the same memory/disk cache entry.
object FirebaseStorageInterceptor : Interceptor {

    /**
     * Strips the rotating ?token= from a Firebase Storage download URL, returning a stable
     * string suitable for use as a Coil memory/disk cache key. Returns null for non-Firebase URLs.
     */
    fun stableKey(url: String): String? {
        if ("firebasestorage.googleapis.com" !in url) return null
        val uri = Uri.parse(url)
        return uri.buildUpon()
            .clearQuery()
            .appendQueryParameter("alt", uri.getQueryParameter("alt") ?: "media")
            .build()
            .toString()
    }

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data

        // Pass non-Firebase URLs straight through — no key rewriting needed.
        if (data !is String) return chain.proceed(chain.request)
        val stableKey = stableKey(data) ?: return chain.proceed(chain.request)

        // Override both caches so neither leaks token-suffixed duplicates.
        val request = chain.request.newBuilder()
            .memoryCacheKey(MemoryCache.Key(stableKey))
            .diskCacheKey(stableKey)
            .build()

        return chain.proceed(request)
    }
}
