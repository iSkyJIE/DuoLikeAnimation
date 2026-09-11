plugins {
    id("com.android.application")
}

android {
    namespace = "com.iskyjie.duolikeanimation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iskyjie.duolikeanimation"
        minSdk = 33
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
