import org.gradle.api.GradleException
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.github.triplet.play")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("io.screenshotbot.screenshot-tests-for-android")
    id("com.google.dagger.hilt.android")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

apply(from = "../gradle/dependency_graph.gradle")

val localProperties = loadLocalProperties()
val missingDropboxKey = "missing_local_key"

android {
    namespace = "journal.gratitude.com.gratitudejournal"
    compileSdk = Versions.COMPILE_SDK

    signingConfigs {
        create("release") {
            val releaseStoreFile = getReleaseStoreFile()
            if (releaseStoreFile != null) {
                storeFile = file(releaseStoreFile)
                storePassword = getRequiredReleaseSecret("RELEASE_STORE_PASSWORD")
                keyAlias = getRequiredReleaseSecret("RELEASE_KEY_ALIAS")
                keyPassword = getRequiredReleaseSecret("RELEASE_KEY_PASSWORD")
            }
        }
    }

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
    }

    packaging {
        resources {
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
        }
    }

    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = project.hasProperty("coverage")
            versionNameSuffix = "-DEBUG"
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseStoreFile = getReleaseStoreFile()
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }


    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.animationsDisabled = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

play {
    defaultToAppBundles.set(true)

    val playServiceAccountFile = getPlayServiceAccountFile()
    if (playServiceAccountFile != null) {
        serviceAccountCredentials.set(file(playServiceAccountFile))
    }
}

tasks.register("verifyReleaseConfig") {
    group = "verification"
    description = "Checks release-only secrets and files before building a release."

    doLast {
        val missing = mutableListOf<String>()

        if (!project.file("google-services.json").isFile) {
            missing += "app/google-services.json"
        }

        if (getDropboxKey() == missingDropboxKey) {
            missing += "DROPBOX_APP_KEY (or DROPBOX_KEY in local.properties)"
        }

        val releaseStoreFile = getReleaseStoreFile()
        if (releaseStoreFile == null) {
            missing += "RELEASE_STORE_FILE"
        } else {
            if (getReleaseSecret("RELEASE_STORE_PASSWORD") == null) {
                missing += "RELEASE_STORE_PASSWORD"
            }
            if (getReleaseSecret("RELEASE_KEY_ALIAS") == null) {
                missing += "RELEASE_KEY_ALIAS"
            }
            if (getReleaseSecret("RELEASE_KEY_PASSWORD") == null) {
                missing += "RELEASE_KEY_PASSWORD"
            }
        }

        if (missing.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Release inputs are missing:")
                    missing.forEach { appendLine(" - $it") }
                }.trimEnd()
            )
        }
    }
}

tasks.register("verifyPlayPublisherConfig") {
    group = "verification"
    description = "Checks Play publishing credentials before upload or promotion."

    doLast {
        val playServiceAccountFile = getPlayServiceAccountFile()
        if (playServiceAccountFile == null) {
            throw GradleException("Missing required Play publishing secret: PLAY_SERVICE_ACCOUNT_FILE")
        }

        if (!file(playServiceAccountFile).isFile) {
            throw GradleException("Play publishing credentials file does not exist: $playServiceAccountFile")
        }
    }
}

listOf("bundleRelease", "assembleRelease").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn("verifyReleaseConfig")
    }
}

listOf("publishReleaseBundle", "publishBundle").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn("verifyReleaseConfig", "verifyPlayPublisherConfig")
    }
}

listOf("promoteReleaseArtifact", "promoteArtifact").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn("verifyPlayPublisherConfig")
    }
}

dependencies {
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
    implementation(Libraries.androidx_room_runtime)
    implementation(Libraries.androidx_room_ktx)
    implementation(Libraries.androidx_room_paging)
    kapt(Libraries.androidx_room_compiler)

    implementation(Libraries.androidx_livedata_ktx)
    implementation(Libraries.androidx_lifecycle_runtime_ktx)
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

    implementation(Libraries.rxjava)
    implementation(Libraries.rxandroid)
    implementation(Libraries.rxbinding)

    implementation(Libraries.mavericks)
    implementation(Libraries.mavericks_mocking)

    implementation(platform(Libraries.firebase_bom))
    implementation(Libraries.firebase_analytics_ktx)
    implementation(Libraries.firebase_crashlytics)

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
    testImplementation(TestLibraries.mavericks_testing)
    testImplementation(TestLibraries.truth)
    testImplementation(TestLibraries.robolectric)
    testImplementation(TestLibraries.androidx_test_core_ktx)
    testImplementation(TestLibraries.androidx_work_testing)

    androidTestImplementation(TestLibraries.androidx_test_runner)
    androidTestImplementation(TestLibraries.androidx_arch_testing)
    androidTestImplementation(TestLibraries.hamcrest)
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
    androidTestImplementation(TestLibraries.androidx_test_rules)
    androidTestImplementation(TestLibraries.kotlin_coroutines_test)
    androidTestImplementation(TestLibraries.mavericks_testing)
    androidTestImplementation(TestLibraries.mockito_android)
    androidTestImplementation(TestLibraries.androidx_work_testing)
    androidTestImplementation(TestLibraries.hilt_android_testing)
    kaptAndroidTest(Libraries.hilt_compiler)
    debugImplementation(TestLibraries.androidx_test_core_ktx)

    androidTestUtil(TestLibraries.test_orchestrator)
}

fun getVersionName(): String {
    return "${Versions.MAJOR}.${Versions.MINOR}.${Versions.PATCH}"
}

fun getDropboxKey(): String {
    return getReleaseSecret("DROPBOX_APP_KEY")
        ?: getReleaseSecret("DROPBOX_KEY")
        ?: missingDropboxKey
}

fun getReleaseStoreFile(): String? {
    return getReleaseSecret("RELEASE_STORE_FILE")
}

fun getPlayServiceAccountFile(): String? {
    return getReleaseSecret("PLAY_SERVICE_ACCOUNT_FILE")
}

fun getRequiredReleaseSecret(name: String): String {
    return getReleaseSecret(name)
        ?: throw GradleException("Missing required release secret: $name")
}

fun getReleaseSecret(name: String): String? {
    return System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(name)?.takeIf { it.isNotBlank() }
}

fun loadLocalProperties(): Properties {
    val localPropertiesFile = rootProject.file("local.properties")
    return Properties().apply {
        if (localPropertiesFile.isFile) {
            localPropertiesFile.inputStream().use(::load)
        }
    }
}
