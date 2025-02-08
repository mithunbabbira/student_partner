plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
}

android {
    namespace = "com.babbira.studentspartner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.babbira.studentspartner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.9"
        apiVersion = "1.9"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    // Use BoM (Bill of Materials) for Firebase
    implementation(platform(libs.firebase.bom))

    // Then declare Firebase dependencies without versions
    implementation(libs.google.firebase.analytics.ktx)
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.com.google.firebase.firebase.firestore.ktx)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.google.firebase.crashlytics.ktx)

    // Explicitly enforce firebase-common version from BoM (Optional, try removing if still failing)
    // implementation("com.google.firebase:firebase-common")

    // Google Sign In
    implementation(libs.play.services.auth)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.googleid)

    // Material Design
    implementation(libs.material)

    // MultiDex
    implementation(libs.androidx.multidex)

    // Circle ImageView for round profile picture
    implementation(libs.circleimageview)

    // Material Design components
    implementation(libs.material.v1110)

    implementation (libs.androidx.coordinatorlayout)

    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

}