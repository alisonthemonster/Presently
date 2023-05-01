plugins {
    id("com.android.library")
    id("kotlin-android")
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

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.5"
    }
    namespace = "com.presently.ui"

    dependencies {
        implementation(Libraries.kotlin_stdlib)
        implementation(Libraries.androidx_core_ktx)
        implementation(Libraries.material)
        implementation(Libraries.compose_ui)
        implementation(Libraries.compose_material)
        implementation(Libraries.compose_material3)
        implementation(Libraries.compose_tooling)
    }
}
