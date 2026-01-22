# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Retrofit
-keepattributes Signature, Exceptions

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep data classes used with API
-keep class com.dnovichkov.yadiskgallery.data.model.** { *; }
-keep class com.dnovichkov.yadiskgallery.domain.model.** { *; }

# Keep Coil
-dontwarn coil.**

# Keep ExoPlayer (Media3)
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Yandex Auth SDK
-keep class com.yandex.authsdk.** { *; }
-dontwarn com.yandex.authsdk.**

# Keep DataStore
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Suppress warnings for missing classes
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
