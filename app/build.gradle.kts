import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "kv.apps.taskmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "kv.apps.taskmanager"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
        signingConfigs {
            getByName("debug") {
                storeFile = file("C:\\Users\\lnkr\\.android\\debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }

            create("release") {
                storeFile = file("plantaskkeystore.jks")
                storePassword = "Lenkorr1239#"
                keyAlias = "plantask"
                keyPassword = "Lenkorr1239#"
            }
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                signingConfig = signingConfigs.getByName("release")
            }
        }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation ("androidx.compose.material3:material3:1.1.2")
    implementation ("com.google.android.material:material:1.12.0")

    //theming support
    implementation ("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    //coil
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")

    // Dagger Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Jetpack Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // Livedata
    implementation("androidx.compose.runtime:runtime-livedata:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")


    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-functions")


    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:21.1.0")


    // Material3
    implementation(libs.material3)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.v111)

    // Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

