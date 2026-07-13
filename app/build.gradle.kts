plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

android {
    namespace = "dev.tgtgetter"
    compileSdk = 36

    signingConfigs {
        create("wearable") {
            storeFile = file("signing/realzhixue.p12")
            storePassword = "realzhixue-local"
            keyAlias = "realzhixue"
            keyPassword = "realzhixue-local"
            storeType = "PKCS12"
        }
    }

    defaultConfig {
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    flavorDimensions += "product"
    productFlavors {
        create("getter") {
            dimension = "product"
            applicationId = "dev.tgtgetter"
        }
        create("sync") {
            dimension = "product"
            applicationId = "cn.seedsoft.realzhixue"
        }
    }

    buildFeatures { compose = true }

    buildTypes {
        debug { signingConfig = signingConfigs.getByName("wearable") }
        release { signingConfig = signingConfigs.getByName("wearable") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    add("syncImplementation", files("libs/xms-wearable-lib_1.4_release.aar"))
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.miuix.ui)
    debugImplementation(compose.uiTooling)
    testImplementation(libs.junit)
}
