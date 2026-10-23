# Keep source line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Kotlin Reflection & Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Retrofit
-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$*
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson & Network DTOs
-keepattributes Signature
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.misterio.core.network.dto.** { *; }

# Strip verbose logs in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepattributes *Annotation*,Inherited
-keepclassmembers class **$$serializer {
    public static final **$$serializer INSTANCE;
}
-keepclassmembers class * {
    *** Companion;
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.misterio.core.database.entity.** { *; }

# Domain Models
-keep class com.misterio.core.domain.model.** { *; }