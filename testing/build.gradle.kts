plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.facebook.testing.screenshot")
    id("dagger.hilt.android.plugin")
}

android {
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    namespace = "com.presently.testing"
}

dependencies {
    implementation(Libraries.kotlin_stdlib)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_compat)
    implementation(Libraries.material)
    implementation(TestLibraries.androidx_arch_testing)

    implementation(Libraries.hilt)
    implementation(TestLibraries.hilt_android_testing)
    kapt(Libraries.hilt_compiler)

    debugImplementation(TestLibraries.fragment_testing)
    debugImplementation(project(":ui"))
}