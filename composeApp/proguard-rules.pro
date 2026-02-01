# R8 Obfuscation Rules for ZorroExpense

# ==== General Rules ====
# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==== Kotlin ====
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# ==== Serialization ====
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==== Firebase ====
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep interface com.google.firebase.** { *; }
-keep interface com.google.android.gms.** { *; }

# Firebase Firestore specific
-keep class dev.gitlive.firebase.** { *; }
-keepclassmembers class dev.gitlive.firebase.** {
    *** get(...);
    *** set(...);
}

# ==== Compose ====
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    *** invoke(...);
}

-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }

# ==== Navigation ====
-keep class androidx.navigation.** { *; }
-keep interface androidx.navigation.** { *; }

# ==== Ktor ====
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-keepclassmembers class io.ktor.** {
    *** get(...);
    *** set(...);
}

# Ktor debug detector uses JDK management APIs not available on Android
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# ==== Coil ====
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ==== Application Domain Models ====
-keep class com.marquis.zorroexpense.domain.** { *; }
-keep interface com.marquis.zorroexpense.domain.** { *; }

# ==== Application DTOs (for Serialization) ====
-keep class com.marquis.zorroexpense.data.** { *; }
-keep interface com.marquis.zorroexpense.data.** { *; }

# ==== Enum Classes ====
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static ** from(java.lang.String);
}

# ==== Remove Logging (Optional) ====
# Uncomment to strip Log.d, Log.v calls in release builds
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
# }