plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("io.screenshotbot.screenshot-tests-for-android")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.presently.sharing"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        testInstrumentationRunner = "com.presently.sharing.CustomTestRunner"
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

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
        }
    }
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":mavericks_utils"))
    implementation(project(":strings"))
    implementation(project(":logging"))

    implementation(Libraries.kotlin_stdlib)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_compat)
    implementation(Libraries.androidx_constraint_layout)
    implementation(Libraries.androidx_viewmodel_ktx)
    implementation(Libraries.androidx_lifecycle_runtime_ktx)
    implementation(Libraries.androidx_recycler_view)
    implementation(Libraries.material)

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

    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.mavericks_testing)
    testImplementation(TestLibraries.truth)

    debugImplementation(project(":testing")) //needed for the hilt test activity
    androidTestImplementation(TestLibraries.hamcrest)
    androidTestImplementation(TestLibraries.androidx_test_junit)
    androidTestImplementation(TestLibraries.androidx_test_core_ktx)
    androidTestImplementation(TestLibraries.androidx_test_espresso_core)
    androidTestImplementation(TestLibraries.androidx_test_espresso_contrib)
    androidTestImplementation(TestLibraries.androidx_test_espresso_intents)
    androidTestImplementation(TestLibraries.androidx_test_rules)
    androidTestImplementation(TestLibraries.mavericks_testing)
    androidTestImplementation(TestLibraries.mockito_android)
    androidTestImplementation(TestLibraries.hilt_android_testing)
    kaptAndroidTest(Libraries.hilt_compiler)
}
