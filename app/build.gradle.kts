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
                val dbgStoreFile = (project.findProperty("DEBUG_STORE_FILE") as String?) ?: System.getenv("DEBUG_STORE_FILE")
                val dbgStorePassword = (project.findProperty("DEBUG_STORE_PASSWORD") as String?) ?: System.getenv("DEBUG_STORE_PASSWORD")
                val dbgKeyAlias = (project.findProperty("DEBUG_KEY_ALIAS") as String?) ?: System.getenv("DEBUG_KEY_ALIAS")
                val dbgKeyPassword = (project.findProperty("DEBUG_KEY_PASSWORD") as String?) ?: System.getenv("DEBUG_KEY_PASSWORD")
                if (!dbgStoreFile.isNullOrBlank() && !dbgStorePassword.isNullOrBlank() && !dbgKeyAlias.isNullOrBlank() && !dbgKeyPassword.isNullOrBlank()) {
                    storeFile = file(dbgStoreFile)
                    storePassword = dbgStorePassword
                    keyAlias = dbgKeyAlias
                    keyPassword = dbgKeyPassword
                }
            }

            create("release") {
                val relStoreFile = (project.findProperty("RELEASE_STORE_FILE") as String?) ?: System.getenv("RELEASE_STORE_FILE")
                val relStorePassword = (project.findProperty("RELEASE_STORE_PASSWORD") as String?) ?: System.getenv("RELEASE_STORE_PASSWORD")
                val relKeyAlias = (project.findProperty("RELEASE_KEY_ALIAS") as String?) ?: System.getenv("RELEASE_KEY_ALIAS")
                val relKeyPassword = (project.findProperty("RELEASE_KEY_PASSWORD") as String?) ?: System.getenv("RELEASE_KEY_PASSWORD")

                if (!relStoreFile.isNullOrBlank() && !relStorePassword.isNullOrBlank() && !relKeyAlias.isNullOrBlank() && !relKeyPassword.isNullOrBlank()) {
                    storeFile = file(relStoreFile)
                    storePassword = relStorePassword
                    keyAlias = relKeyAlias
                    keyPassword = relKeyPassword
                } else {
                    logger.lifecycle("[Signing] Release signing config not set: missing RELEASE_* properties. The release build will be unsigned.")
                }
            }
        }

        buildTypes {
            release {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                val hasReleaseSigning = signingConfigs.findByName("release")?.storeFile != null
                if (hasReleaseSigning) {
                    signingConfig = signingConfigs.getByName("release")
                }
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
    // Room
    val roomVersion = "2.7.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    
    // Network Connectivity Monitoring 
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    
    // Gson for type converters
    implementation("com.google.code.gson:gson:2.10.1")

    // Hilt Work (for background sync)
    implementation("androidx.hilt:hilt-work:1.2.0")

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

    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    implementation("io.github.vanpra.compose-material-dialogs:core:0.9.0")


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
    testImplementation("junit:junit:4.13.2")
    kaptTest(libs.hilt.android.compiler)
    kaptTest("androidx.room:room-compiler:2.7.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}



// Warn if FCM_SERVER_KEY is present in client build environment
val fcmServerKey = (project.findProperty("FCM_SERVER_KEY") as String?) ?: System.getenv("FCM_SERVER_KEY")
if (!fcmServerKey.isNullOrBlank()) {
    logger.warn("[Security] FCM_SERVER_KEY detected in client build environment. Do NOT store or use server keys in the Android app. Remove it and keep server credentials only on the backend (e.g., Cloud Functions/Secret Manager).")
}
