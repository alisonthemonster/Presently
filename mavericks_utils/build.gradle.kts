plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
        compileOnly(Libraries.assisted_inject_annotations)
        kapt(Libraries.assisted_inject_processor)
        implementation(Libraries.hilt)
        kapt(Libraries.hilt_compiler)
        implementation(Libraries.hilt_viewmodel)
        kapt(Libraries.hilt_android_compiler)
    }
}