# Add project specific ProGuard rules here.

# Preserve line numbers in stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase Firestore — keep model classes used with toObject()
-keep class dk.zlatan.flotmand.model.** { *; }

# Firebase Auth
-keepattributes Signature
-keepattributes *Annotation*

# Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Google Places / Maps
-keep class com.google.android.libraries.places.** { *; }
-keep class com.google.android.gms.maps.** { *; }
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.mikepenz.aboutlibraries.** { *; }
