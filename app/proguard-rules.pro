-keep class org.opencv.** { *; }
-keepclassmembers class org.opencv.** { *; }
-dontwarn org.opencv.**

# Métodos nativos nunca devem ser renomeados
-keepclasseswithmembernames class * {
    native <methods>;
}