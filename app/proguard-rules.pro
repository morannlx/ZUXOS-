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
-keep class * extends android.app.Application {
    <init>();
    void attachBaseContext(android.content.Context);
}
-keep class * extends android.app.Activity
