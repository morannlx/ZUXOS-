import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.isFile }
        ?.inputStream()
        ?.use { load(it) }
}

android {
    signingConfigs {
        localProperties.getProperty("RELEASE_STORE_FILE")?.let { releaseStoreFile ->
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }
    namespace = gropify.project.app.packageName
    compileSdk = gropify.project.android.compileSdk

    defaultConfig {
        applicationId = gropify.project.app.packageName
        minSdk = gropify.project.android.minSdk
        targetSdk = gropify.project.android.targetSdk
        versionName = gropify.project.app.versionName
        versionCode = gropify.project.app.versionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug {
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
    lint { checkReleaseBuilds = false }

    // androidResources.additionalParameters += listOf("--allow-reserved-package-id", "--package-id", "0x64")
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.rovo89.xposed.api)
    ksp(libs.yukihook.compiler)
    implementation(platform(libs.yukihook.bom))
    implementation(libs.yukihook.core)
    implementation(libs.yukihook.runtime.xposed82)
    implementation(libs.yukihook.extension.kavaref)

    // Optional: KavaRef (https://github.com/HighCapable/KavaRef)
    implementation(platform(libs.kavaref.bom))
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.android)
    implementation(libs.kavaref.extension)

    // Optional: Hikage (https://github.com/BetterAndroid/Hikage)
    ksp(libs.hikage.compiler)
    implementation(libs.hikage.core)
    implementation(libs.hikage.extension)
    implementation(libs.hikage.widget.androidx)
    implementation(libs.hikage.widget.material)

    // Optional: BetterAndroid (https://github.com/BetterAndroid/BetterAndroid)
    implementation(libs.betterandroid.ui.component)
    implementation(libs.betterandroid.ui.component.adapter)
    implementation(libs.betterandroid.ui.extension)
    implementation(libs.betterandroid.system.extension)

    implementation(libs.drawabletoolbox)

    // AndroidLiquidGlass (https://github.com/Kyant0/AndroidLiquidGlass)
    implementation(libs.backdrop)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.material)
    implementation(libs.media3.exoplayer)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

configurations.configureEach {
    resolutionStrategy {
        // YukiHook 1.5.0 的传递依赖会把 core 升到 1.19.0，而它要求 AGP 9.1+。
        force(
            "androidx.core:core:1.17.0",
            "androidx.core:core-ktx:1.17.0",
            "androidx.activity:activity:1.10.1",
            "androidx.activity:activity-ktx:1.10.1",
            "androidx.activity:activity-compose:1.10.1",
        )
    }
}
