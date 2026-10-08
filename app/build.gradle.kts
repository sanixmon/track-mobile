import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Load centralized version properties
val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        FileInputStream(versionPropsFile).use { this.load(it) }
    }
}
val appVersionCode = (versionProps.getProperty("VERSION_BUILD") ?: "19").toInt()
val appVersionName = "${versionProps.getProperty("VERSION_MAJOR") ?: "2"}.${versionProps.getProperty("VERSION_MINOR") ?: "7"}.${versionProps.getProperty("VERSION_PATCH") ?: "0"}"

// Default API base URL = production. Override per build, e.g.
//   ./gradlew assembleDebug -PAPI_BASE_URL=http://192.168.1.10:3005
val apiBaseUrl: String = (project.findProperty("API_BASE_URL") as String?)?.trim()?.takeIf { it.isNotEmpty() }
    ?: "https://qr.evrenhouse.online"

android {
    namespace = "com.evrenhouse.trackscooter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.evrenhouse.trackscooter"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }
    signingConfigs {
        create("release") {
            val rawPath = System.getenv("CM_KEYSTORE_PATH")
                ?: System.getenv("RELEASE_KEYSTORE_PATH")
                ?: project.findProperty("RELEASE_KEYSTORE_PATH") as String?
                ?: "keystore/release.jks"

            val targetPath = if (file(rawPath).isAbsolute) file(rawPath) else rootProject.file(rawPath)
            storeFile = targetPath.absoluteFile
            storePassword = System.getenv("CM_KEYSTORE_PASSWORD")
                ?: System.getenv("RELEASE_KEYSTORE_PASSWORD")
                ?: project.findProperty("RELEASE_KEYSTORE_PASSWORD") as String?
                ?: "TrackScooter2026!"
            keyAlias = System.getenv("CM_KEY_ALIAS")
                ?: System.getenv("RELEASE_KEY_ALIAS")
                ?: project.findProperty("RELEASE_KEY_ALIAS") as String?
                ?: "trackscooter"
            keyPassword = System.getenv("CM_KEY_PASSWORD")
                ?: System.getenv("RELEASE_KEY_PASSWORD")
                ?: project.findProperty("RELEASE_KEY_PASSWORD") as String?
                ?: "TrackScooter2026!"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.concurrent.futures.ktx)

    // QR: ML Kit decode (camera + image) & ZXing generate
    implementation(libs.mlkit.barcode)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.zxing.core)

    // Image loading (QR thumbnails / gallery preview)
    implementation(libs.coil.compose)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
