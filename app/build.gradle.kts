import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.aboutlibraries.plugin)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }
val googleServerClientIdDebug = localProperties["GOOGLE_SERVER_CLIENT_ID_DEBUG"] as String? ?: ""
val googleServerClientIdRelease = localProperties["GOOGLE_SERVER_CLIENT_ID_RELEASE"] as String? ?: ""
val googleMapsApiKey = localProperties["GOOGLE_MAPS_API_KEY"] as String? ?: ""

val commitCount = "git rev-list --count HEAD".trimIndent().let {
    Runtime.getRuntime().exec(it.split(" ").toTypedArray())
        .inputStream.bufferedReader().readText().trim().toIntOrNull() ?: 1
}

android {
    namespace = "dk.zlatan.flotmand"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties["RELEASE_STORE_FILE"] as String?
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = localProperties["RELEASE_STORE_PASSWORD"] as String
                keyAlias = localProperties["RELEASE_KEY_ALIAS"] as String
                keyPassword = localProperties["RELEASE_KEY_PASSWORD"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "dk.zlatan.flotmand"
        minSdk = 30
        targetSdk = 36
        versionCode = commitCount
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$googleMapsApiKey\"")
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
        resValue("string", "google_maps_key", googleMapsApiKey)
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Flotmand - debug")
            buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"$googleServerClientIdDebug\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"$googleServerClientIdRelease\"")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

hilt {
    enableAggregatingTask = false
}

// Disable ART baseline profile merging from libraries.
// Without this, Compose and other libraries embed baseline profiles that
// cause INSTALL_BASELINE_PROFILE_FAILED on some devices/emulators.
tasks.matching { it.name.contains("ArtProfile") }.configureEach {
    enabled = false
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.hilt.android)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.ui)
    implementation(libs.play.services.auth)
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.github.yalantis:ucrop:2.2.9")
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.aboutlibraries.core)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation(libs.javapoet)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Google Maps for LatLng and location services
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Google Places API for address autocomplete
    implementation("com.google.android.libraries.places:places:4.1.0")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:4.4.1")

    implementation("androidx.appcompat:appcompat:1.7.1")


    implementation(libs.reorderable)
    implementation(libs.androidx.datastore.preferences)
    implementation("com.airbnb.android:lottie-compose:6.6.6")
    implementation(libs.androidx.browser)
}
