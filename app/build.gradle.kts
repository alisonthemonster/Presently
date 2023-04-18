import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.facebook.testing.screenshot")
    id("com.google.dagger.hilt.android")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

apply(from = "../gradle/dependency_graph.gradle")

android {
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        applicationId = "journal.gratitude.com.gratitudejournal"
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        versionCode = Versions.APP_VERSION_CODE
        versionName = getVersionName()

        testInstrumentationRunner = "journal.gratitude.com.gratitudejournal.testUtils.AppCustomTestRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }

        val dropboxKey = getDropboxKey()
        buildConfigField("String", "DROPBOX_APP_KEY", "\"${dropboxKey}\"")
        manifestPlaceholders["dropboxAppKey"] = dropboxKey
    }

    buildFeatures {
        dataBinding = true //TODO are we still using this?
        viewBinding = true
        compose = true
    }

    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = project.hasProperty("coverage")
            versionNameSuffix = "-DEBUG"
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.5"
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }


    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.animationsDisabled = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }

    namespace = "journal.gratitude.com.gratitudejournal"
}

dependencies {
    implementation(project(":logging"))
    implementation(project(":settings"))
    implementation(project(":strings"))
    implementation(project(":ui"))
    implementation(project(":coroutine_utils"))

    implementation(Libraries.kotlin_stdlib)
    implementation(Libraries.androidx_compat)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_constraint_layout)
    implementation(Libraries.androidx_preference_ktx)
    implementation(Libraries.androidx_recycler_view) //todo probably remove
    implementation(Libraries.viewmodel_savedstate)
    implementation(Libraries.androidx_fragment)
    implementation(Libraries.androidx_biometric)
    implementation(Libraries.androidx_work_runtime_ktx)
    implementation(Libraries.play_core)
    implementation(Libraries.androidx_room_runtime)
    implementation(Libraries.androidx_room_ktx)
    kapt(Libraries.androidx_room_compiler)

    implementation(Libraries.compose_ui)
    implementation(Libraries.compose_material)
    implementation(Libraries.compose_material3)
    implementation(Libraries.compose_tooling)
    implementation(Libraries.compose_viewmodel)
    implementation(Libraries.compose_navigation)
    implementation(Libraries.compose_hilt_navigation)
    implementation(Libraries.compose_accompanist_navigation_animation)
    implementation(Libraries.compose_accompanist_systemuicontroller)
    implementation(Libraries.compose_accompanist_insetsui)
    implementation(Libraries.compose_interop_view_binding)
    implementation(Libraries.compose_constraint)

    implementation(Libraries.androidx_viewmodel_ktx)
    kapt(Libraries.androidx_lifecycle_compiler)

    implementation(Libraries.three_ten_abp)
    implementation(Libraries.kotlin_coroutines_android)
    implementation(Libraries.material)
    implementation(Libraries.play_services_oss_licenses)
    implementation(Libraries.compact_calendar_view)
    implementation(Libraries.dropbox_sdk)
    implementation(Libraries.apache_text)
    implementation(Libraries.apache_csv)

    //dependency injection
    implementation(Libraries.dagger)
    kapt(Libraries.dagger_compiler)
    implementation(Libraries.dagger_android_support)
    kapt(Libraries.dagger_android_processor)
    implementation(Libraries.hilt)
    kapt(Libraries.hilt_compiler)
    kapt(Libraries.hilt_android_compiler)
    implementation(Libraries.androidx_hilt_work)

    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.three_ten_abp) {
        exclude(group = "com.jakewharton.threetenabp", module = "threetenabp")
    }
    testImplementation(TestLibraries.androidx_room_testing)
    testImplementation(TestLibraries.mockito_kotlin)
    testImplementation(TestLibraries.androidx_arch_testing)
    testImplementation(TestLibraries.kotlin_test_junit)
    testImplementation(TestLibraries.kotlin_coroutines_test)
    testImplementation(TestLibraries.truth)

    androidTestImplementation(project(":testing"))
    androidTestImplementation(TestLibraries.androidx_test_runner)
    androidTestImplementation(TestLibraries.androidx_arch_testing)
    androidTestImplementation(TestLibraries.espresso_core)
    androidTestImplementation(TestLibraries.espresso_contrib)
    androidTestImplementation(TestLibraries.androidx_room_testing)
    androidTestImplementation(TestLibraries.androidx_test_junit)
    androidTestImplementation(TestLibraries.kotlin_test_junit)
    androidTestImplementation(TestLibraries.androidx_test_espresso_intents)
    androidTestImplementation(TestLibraries.truth)
    androidTestImplementation(TestLibraries.mockito_kotlin)
    androidTestImplementation(TestLibraries.three_ten_abp)
    androidTestImplementation(TestLibraries.androidx_test_uiautomator)
    androidTestImplementation(TestLibraries.kotlin_coroutines_test)
    androidTestImplementation(TestLibraries.androidx_work_testing)
    androidTestImplementation(TestLibraries.hilt_android_testing)
    androidTestImplementation(TestLibraries.compose_ui_testing)
    androidTestImplementation(TestLibraries.compose_ui_testing_manifest)
    kaptAndroidTest(Libraries.hilt_compiler)
    debugImplementation(project(":testing")) //needed for the hilt test activity
    debugImplementation(TestLibraries.fragment_testing)

    androidTestUtil(TestLibraries.test_orchestrator)
}

fun getVersionName(): String {
    return "${Versions.MAJOR}.${Versions.MINOR}.${Versions.PATCH}"
}

fun getDropboxKey(): String {
    val localPropsKey = gradleLocalProperties(rootDir).getProperty("DROPBOX_KEY") ?: "missing_local_key"
    return System.getenv("DROPBOX_APP_KEY") ?: localPropsKey
}
