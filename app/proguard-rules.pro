# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static *** throwUninitializedProperty(...);
    public static *** throwUninitializedPropertyAccessException(...);
}

-keep class me.inkdye.zuxos.application.DefaultApplication { *; }
-keep class me.inkdye.zuxos.corepatch.** { *; }
-keep class me.inkdye.zuxos.hook.systemframework.** { *; }
-dontwarn java.lang.reflect.AnnotatedType
-keep class * extends android.app.Application {
    <init>();
    void attachBaseContext(android.content.Context);
}
-keep class * extends android.app.Activity
-keep class * extends android.service.wallpaper.WallpaperService
