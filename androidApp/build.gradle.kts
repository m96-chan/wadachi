plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Release signing is configured only when the keystore is provided (CI sets these from secrets).
val releaseKeystoreFile = providers.environmentVariable("WADACHI_KEYSTORE_FILE")

android {
    namespace = "io.github.m96chan.wadachi"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.m96chan.wadachi"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = providers.gradleProperty("wadachi.versionCode").map(String::toInt).getOrElse(1)
        versionName = providers.gradleProperty("wadachi.versionName").getOrElse("0.1.0")
    }

    signingConfigs {
        if (releaseKeystoreFile.isPresent) {
            create("release") {
                storeFile = file(releaseKeystoreFile.get())
                storePassword = providers.environmentVariable("WADACHI_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("WADACHI_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("WADACHI_KEY_PASSWORD").get()
            }
        }
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
}
