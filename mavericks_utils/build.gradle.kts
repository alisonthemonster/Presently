plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.presently.mavericks_utils"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = project.hasProperty("coverage")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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

    dependencies {
        implementation(Libraries.kotlin_stdlib)
        implementation(Libraries.mavericks)
        implementation(Libraries.mavericks_mocking)

        //dependency injection
        implementation(Libraries.dagger)
        kapt(Libraries.dagger_compiler)
        implementation(Libraries.dagger_android_support)
        kapt(Libraries.dagger_android_processor)
        implementation(Libraries.hilt)
        kapt(Libraries.hilt_compiler)
        kapt(Libraries.hilt_android_compiler)
    }
}
