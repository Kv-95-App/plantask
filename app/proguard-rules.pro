-keepattributes SourceFile,LineNumberTable

# Gson TypeToken support
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# Room database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keepclassmembers class * {
    @androidx.room.* *;
}

# Your data classes
-keep class kv.apps.taskmanager.data.local.entity.** { *; }
-keep class kv.apps.taskmanager.data.remote.model.** { *; }
-keep class kv.apps.taskmanager.domain.model.** { *; }

# Hilt/Dagger
-keep class dagger.hilt.** { *; }
-keep class dagger.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Kotlin
-dontwarn kotlin.**
-keep class kotlinx.coroutines.** { *; }