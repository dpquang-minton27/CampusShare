plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Default: local Fake Repository, no google-services.json required.
// Firebase: ./gradlew :app:assembleDebug -PcampusshareBackend=firebase
val useFirebase = providers.gradleProperty("campusshareBackend").orNull == "firebase"
val useFirebaseEmulator = useFirebase &&
    providers.gradleProperty("campusshareEmulator").orNull == "true"
if (useFirebase) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.campusshare.app"
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    defaultConfig {
        applicationId = "com.campusshare.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.3.0-bc3"
        buildConfigField("boolean", "USE_FIREBASE", useFirebase.toString())
        buildConfigField("boolean", "USE_FIREBASE_EMULATOR", useFirebaseEmulator.toString())
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.13.0")
    // Firebase Auth pulls Fragment 1.1.0, which is incompatible with Activity Result.
    implementation("androidx.fragment:fragment:1.8.9")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0")

    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // Do not add firebase-storage until the team's Blaze decision is recorded.

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}
