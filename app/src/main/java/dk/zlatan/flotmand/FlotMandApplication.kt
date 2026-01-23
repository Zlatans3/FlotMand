package dk.zlatan.flotmand

import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class FlotMandApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        }
    }

    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("selected_language", "da") ?: "da"
        val context = dk.zlatan.flotmand.util.LocaleHelper.setLocale(base, lang)
        super.attachBaseContext(context)
    }
}
