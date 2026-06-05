package dk.zlatan.flotmand

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import dk.zlatan.flotmand.impl.FlotMandFirebaseMessagingService
import dk.zlatan.flotmand.util.FirebaseStorageInterceptor
import java.util.Locale

@HiltAndroidApp
class FlotMandApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(300)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .components {
                add(FirebaseStorageInterceptor)
            }
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize Places API
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        if (apiKey.isNotEmpty() && !Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FlotMandFirebaseMessagingService.CHANNEL_ID,
                "Events & Polls",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for new events and polls"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("selected_language", "da") ?: "da"
        val context =
            dk.zlatan.flotmand.util.LocaleHelper
                .setLocale(base, lang)
        super.attachBaseContext(context)
    }
}
