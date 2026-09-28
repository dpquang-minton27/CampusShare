plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.campusshare.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.campusshare.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// BC1 chỉ dùng Android SDK. Thêm Firebase/Room và thư viện UI sau khi chốt data contract.
