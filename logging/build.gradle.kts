plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.presently.logging"
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        implementation(Libraries.androidx_core_ktx)

        api(platform(Libraries.firebase_bom))
        api(Libraries.firebase_analytics_ktx)
        api(Libraries.firebase_crashlytics)

        implementation(Libraries.dagger)
        kapt(Libraries.dagger_compiler)
        implementation(Libraries.dagger_android_support)
        kapt(Libraries.dagger_android_processor)
        implementation(Libraries.hilt)
        kapt(Libraries.hilt_compiler)
        kapt(Libraries.hilt_android_compiler)

        testImplementation(TestLibraries.junit)
        testImplementation(TestLibraries.mockito_kotlin)
        testImplementation(TestLibraries.robolectric)
        testImplementation(TestLibraries.truth)
    }
}
