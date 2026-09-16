# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Preserve Data Models and Serializable classes
-keep class com.example.model.** { *; }
-keep class com.example.data.** { *; }

# Keep Moshi JSON models
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Keep Room Database entities and DAOs
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep Google Play Ads
-keep public class com.google.android.gms.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**
