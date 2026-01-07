package dk.zlatan.flotmand

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlotMandApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        }
    }
}
