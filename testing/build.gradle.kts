plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("io.screenshotbot.screenshot-tests-for-android")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.presently.testing"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
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
}

dependencies {
    implementation(Libraries.kotlin_stdlib)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_compat)
    implementation(Libraries.material)
    implementation(TestLibraries.androidx_test_core_ktx)

    implementation(Libraries.hilt)
    kapt(Libraries.hilt_compiler)
    debugImplementation(project(":ui"))
}
