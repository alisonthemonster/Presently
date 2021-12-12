plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":logging"))

    implementation(Libraries.kotlin_stdlib)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_compat)
    implementation(Libraries.androidx_preference_ktx)

    implementation(Libraries.three_ten_abp)
    implementation(Libraries.dropbox_sdk)

    //dependency injection
    implementation(Libraries.dagger)
    kapt(Libraries.dagger_compiler)
    implementation(Libraries.dagger_android_support)
    kapt(Libraries.dagger_android_processor)
    implementation(Libraries.hilt)
    kapt(Libraries.hilt_compiler)
    implementation(Libraries.hilt_viewmodel)
    kapt(Libraries.hilt_android_compiler)

    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.truth)
}