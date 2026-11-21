

// MSAL --> determine keystore hash for manifest
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Base64

val keystoreFile = rootProject.file("canteenchecker.keystore")
val keystorePassword = "canteenchecker"
val signingKeyAlias = "canteenchecker"
val signingKeyPassword = "canteenchecker"

val signingCertificateHash: String = KeyStore.getInstance("PKCS12").run {
    keystoreFile.inputStream().use { load(it, keystorePassword.toCharArray()) }
    Base64.getEncoder().encodeToString(
        MessageDigest.getInstance("SHA-1").digest(getCertificate(signingKeyAlias).encoded)
    )
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.canteenchecker.consumerapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.canteenchecker.consumerapp"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["signingCertificateHash"] = signingCertificateHash
    }

    // CUSTOM SIGNING
    signingConfigs {
        getByName("debug") {
            storeFile = keystoreFile
            storePassword = keystorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // COMPOSE
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    // VIEWMODEL
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // ICONS
    implementation(libs.androidx.material.icons.core)
    // RETROFIT
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    // NAVIGATION
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    // MSAL
    implementation(libs.msal)
}
