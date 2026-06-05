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
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data

        // Pass non-Firebase URLs straight through — no key rewriting needed.
        if (data !is String || "firebasestorage.googleapis.com" !in data) {
            return chain.proceed(chain.request)
        }

        // Build a stable key from the URL with all query params stripped except
        // `alt=media`, which tells Firebase to return the file bytes rather than metadata.
        val uri = Uri.parse(data)
        val stableKey = uri.buildUpon()
            .clearQuery()
            .appendQueryParameter("alt", uri.getQueryParameter("alt") ?: "media")
            .build()
            .toString()

        // Override both caches so neither leaks token-suffixed duplicates.
        val request = chain.request.newBuilder()
            .memoryCacheKey(MemoryCache.Key(stableKey))
            .diskCacheKey(stableKey)
            .build()

        return chain.proceed(request)
    }
}
