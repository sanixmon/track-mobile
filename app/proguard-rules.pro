# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# kotlinx.serialization
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.evrenhouse.trackscooter.**$$serializer { *; }
-keepclassmembers class com.evrenhouse.trackscooter.** {
    *** Companion;
}
-keepclasseswithmembers class com.evrenhouse.trackscooter.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ML Kit & CameraX
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn androidx.camera.**
