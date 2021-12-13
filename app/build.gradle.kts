import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.facebook.testing.screenshot")
    id("dagger.hilt.android.plugin")
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

        val dropboxKey = getDropboxKey()
        buildConfigField("String", "DROPBOX_APP_KEY", "\"${dropboxKey}\"")
        manifestPlaceholders["dropboxAppKey"] = dropboxKey
    }

    buildFeatures {
        dataBinding = true //TODO are we still using this?
        viewBinding = true
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

    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.animationsDisabled = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    //needed for kotlin_coroutines_test
    packagingOptions {
        exclude ("META-INF/AL2.0")
        exclude ("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(project(":logging"))
    implementation(project(":mavericks_utils"))
    implementation(project(":settings"))
    implementation(project(":sharing"))
    implementation(project(":strings"))
    implementation(project(":ui"))
    implementation(project(":presently_local_source"))
    implementation(project(":date_utils"))

    implementation(Libraries.kotlin_stdlib)
    implementation(Libraries.androidx_compat)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_constraint_layout)
    implementation(Libraries.androidx_preference_ktx)
    implementation(Libraries.androidx_recycler_view)
    implementation(Libraries.androidx_fragment)
    implementation(Libraries.androidx_biometric)
    implementation(Libraries.androidx_work_runtime_ktx)
    implementation(Libraries.play_core)
    implementation(Libraries.androidx_paging_runtime)
    kapt(Libraries.androidx_room_compiler)

    implementation(Libraries.androidx_livedata_ktx)
    implementation(Libraries.androidx_viewmodel_ktx)
    kapt(Libraries.androidx_lifecycle_compiler)

    //implementation(Libraries.three_ten_abp)
    implementation(Libraries.kotlin_coroutines_android)
    implementation(Libraries.material)
    implementation(Libraries.play_services_oss_licenses)
    implementation(Libraries.compact_calendar_view)
    implementation(Libraries.dropbox_sdk)
    implementation(Libraries.apache_text)
    implementation(Libraries.apache_csv)

    implementation(Libraries.rxjava)
    implementation(Libraries.rxandroid)
    implementation(Libraries.rxbinding)

    implementation(Libraries.mavericks)
    implementation(Libraries.mavericks_mocking)

    //dependency injection
    implementation(Libraries.dagger)
    kapt(Libraries.dagger_compiler)
    implementation(Libraries.dagger_android_support)
    kapt(Libraries.dagger_android_processor)
    implementation(Libraries.hilt)
    kapt(Libraries.hilt_compiler)

    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.three_ten_abp) {
        exclude(group = "com.jakewharton.threetenabp", module = "threetenabp")
    }
    testImplementation(TestLibraries.androidx_room_testing)
    testImplementation(TestLibraries.mockito_kotlin)
    testImplementation(TestLibraries.androidx_arch_testing)
    testImplementation(TestLibraries.kotlin_test_junit)
    testImplementation(TestLibraries.kotlin_coroutines_test)
    testImplementation(TestLibraries.mavericks_testing)
    testImplementation(TestLibraries.truth)

    androidTestImplementation(project(":testing"))
    androidTestImplementation(TestLibraries.androidx_test_runner)
    androidTestImplementation(TestLibraries.androidx_arch_testing)
    androidTestImplementation(TestLibraries.espresso_core)
    androidTestImplementation(TestLibraries.espresso_contrib)
//    androidTestImplementation(TestLibraries.androidx_room_testing)
    androidTestImplementation(TestLibraries.androidx_test_junit)
    androidTestImplementation(TestLibraries.androidx_test_espresso_intents)
    androidTestImplementation(TestLibraries.truth)
    androidTestImplementation(TestLibraries.mockito_kotlin)
    androidTestImplementation(TestLibraries.three_ten_abp)
    androidTestImplementation(TestLibraries.androidx_test_uiautomator)
    androidTestImplementation(TestLibraries.kotlin_coroutines_test)
    androidTestImplementation(TestLibraries.androidx_work_testing)
    androidTestImplementation(TestLibraries.hilt_android_testing)
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
