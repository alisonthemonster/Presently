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

        testInstrumentationRunner = "com.presently.sharing.CustomTestRunner"
        consumerProguardFiles("consumer-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
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

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        exclude ("META-INF/AL2.0")
        exclude ("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(project(":date_utils"))

    //room + paging
    implementation(Libraries.androidx_paging_runtime)
    implementation(Libraries.androidx_room_ktx)
    implementation(Libraries.androidx_room_runtime)
    implementation(Libraries.androidx_paging_runtime)
    kapt(Libraries.androidx_room_compiler)

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
    testImplementation(TestLibraries.kotlin_test_junit)
    testImplementation(TestLibraries.kotlin_coroutines_test)
    testImplementation(TestLibraries.truth)
    testImplementation(TestLibraries.androidx_arch_testing)
    testImplementation(TestLibraries.androidx_room_testing)
    testImplementation(TestLibraries.three_ten_abp) {
        exclude(group = "com.jakewharton.threetenabp", module = "threetenabp")
    }

    androidTestImplementation(TestLibraries.androidx_test_junit)
    androidTestImplementation(TestLibraries.espresso_core)
    androidTestImplementation(TestLibraries.androidx_arch_testing)
    androidTestImplementation(TestLibraries.androidx_room_testing)
    androidTestImplementation(TestLibraries.kotlin_coroutines_test)
}